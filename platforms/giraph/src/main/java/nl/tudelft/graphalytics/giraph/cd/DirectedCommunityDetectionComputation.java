/**
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics.giraph.cd;

import static nl.tudelft.graphalytics.giraph.cd.CommunityDetectionConfiguration.HOP_ATTENUATION;
import static nl.tudelft.graphalytics.giraph.cd.CommunityDetectionConfiguration.MAX_ITERATIONS;
import static nl.tudelft.graphalytics.giraph.cd.CommunityDetectionConfiguration.NODE_PREFERENCE;

import org.apache.giraph.conf.ImmutableClassesGiraphConfiguration;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.edge.EdgeFactory;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.*;

import java.io.IOException;
import java.util.*;

/**
 * Community Detection algorithm
 * Credits: mostly Marcin's code refactored
 * Detect Community using algorithm by methods provided in
 * "Towards Real-Time Community Detection in Large Networks by Ian X.Y. Leung,Pan Hui,Pietro Li,and Jon Crowcroft"
 * Changes
 * - refactored private attributes to CommunityDetectionWritable
 * - refactored very long methods
 * - removed unused attributes.
 * Question
 * - why are there two iteration thresholds?
 *
 * Note: Value on edge is true iff the edge is bidirectional. These edges have double weight in the label selection
 * process.
 *
 * @author Wing Ngai
 * @author Tim Hegeman
 */
public class DirectedCommunityDetectionComputation extends BasicComputation<LongWritable, CDLabel, BooleanWritable, Text> {
    // Load the parameters from the configuration before the compute method to save expensive lookups
	private float nodePreference;
	private float hopAttenuation;
	private int maxIterations;
	
	@Override
	public void setConf(ImmutableClassesGiraphConfiguration<LongWritable, CDLabel, BooleanWritable> conf) {
		super.setConf(conf);
		nodePreference = NODE_PREFERENCE.get(getConf());
		hopAttenuation = HOP_ATTENUATION.get(getConf());
		maxIterations = MAX_ITERATIONS.get(getConf());
	}
	
    @Override
    public void compute(Vertex<LongWritable, CDLabel, BooleanWritable> vertex, Iterable<Text> messages) throws IOException {
        // max iteration, a stopping condition for data-sets which do not converge
        if (this.getSuperstep() > maxIterations+2) {
            vertex.voteToHalt();
            return;
        }

        // send vertex id to outgoing neigbhours, so that all vertices know their incoming edges.
        if (getSuperstep() == 0) {
            for (Edge<LongWritable, BooleanWritable> edge : vertex.getEdges()) {
                LongWritable neighbor = edge.getTargetVertexId();
                sendMessage(neighbor, new Text(String.valueOf(vertex.getId().get())));
            }

	        // Mark all edges as unidirectional
	        for (Edge<LongWritable, BooleanWritable> edge : vertex.getEdges()) {
		        vertex.setEdgeValue(edge.getTargetVertexId(), new BooleanWritable(false));
	        }
        }
        // add incoming edges
        else if  (getSuperstep() == 1) {
            Set<Long> edges = new HashSet<Long>();
            for (Edge<LongWritable, BooleanWritable> edge : vertex.getEdges()) {
                edges.add(edge.getTargetVertexId().get());
            }
            for (Text message : messages) {
                long incomingVertexId = Long.parseLong(message.toString());
                if(!edges.contains(incomingVertexId)) {
                    addEdgeRequest(vertex.getId(), EdgeFactory.create(new LongWritable
                            (incomingVertexId), new BooleanWritable(false)));
                } else {
	                vertex.setEdgeValue(new LongWritable(incomingVertexId), new BooleanWritable(true));
                }
            }
        }
        // start community detection rounds
        else if (this.getSuperstep() == 2) {

            // initialize algorithm, set label as the vertex id, set label score as 1.0
            CDLabel cd = new CDLabel(String.valueOf(vertex.getId().get()), 1.0f);
            vertex.setValue(cd);

            // send initial label to all neighbors
            propagateLabel(vertex);
            return;
        }
        else {
            // label assign
            determineLabel(vertex, messages);
            propagateLabel(vertex);
        }
    }

    /**
     * Propagate label information to neighbors
     */
    private void propagateLabel(Vertex<LongWritable, CDLabel, BooleanWritable> vertex) {
        CDLabel cd = vertex.getValue();
        for (Edge<LongWritable, BooleanWritable> edge : vertex.getEdges()) {
            Text initMessage = new Text(vertex.getId().get() + "," + cd.getLabelName() + "," + cd.getLabelScore() + "," + vertex.getNumEdges());
            sendMessage(edge.getTargetVertexId(), initMessage);
        }
    }

    /**
     * Chooses new label AND updates label score
     * - chose new label based on SUM of Label_score(sum all scores of label X) x f(i')^m, where m is number of edges (ignore edge weight == 1) -> EQ 2
     * - score of a vertex new label is a maximal score from all existing scores for that particular label MINUS delta (specified as input parameter) -> EQ 3
     */
    private void determineLabel(Vertex<LongWritable, CDLabel, BooleanWritable> vertex, Iterable<Text> messages) {

        CDLabel cd = vertex.getValue();
        String oldLabel = cd.getLabelName().toString();

        // fill in the labelAggScoreMap and labelMaxScoreMap from the received messages (by EQ2 step 1)
        Map<String, CDLabelStatistics> labelStatsMap = groupLabelStatistics(vertex, messages);

        // choose label based on the gathered label info (by EQ2 step 2)
        String chosenLabel = chooseLabel(labelStatsMap);
        cd.setLabelName(new Text(chosenLabel));

        // update new label score by EQ3
        float updatedLabelScore = getChosenLabelScore(labelStatsMap, chosenLabel, oldLabel);
        cd.setLabelScore(updatedLabelScore);
    }

    /**
     * Calculate the aggregated score and max score per distinct label. (EQ 2 step 1)
     */
    public Map<String, CDLabelStatistics> groupLabelStatistics(Vertex<LongWritable, CDLabel, BooleanWritable> vertex,
                                                               Iterable<Text> messages) {

        Map<String, CDLabelStatistics> labelStatsMap = new HashMap<String, CDLabelStatistics>();

        // group label statistics
	    LongWritable sourceId = new LongWritable();
        for (Text message : messages) {

            CDMessage cdMsg = CDMessage.FromText(message);
	        sourceId.set(cdMsg.getSourceId());
            String labelName = cdMsg.getLabelName();
            float labelScore = cdMsg.getLabelScore();
            int f = cdMsg.getF();

            float weightedLabelScore = labelScore * (float) Math.pow((double) f, (double) nodePreference);

	        // Double score if edge is bidirectional
	        if (vertex.getEdgeValue(sourceId).get()) {
		        weightedLabelScore *= 2;
	        }

            if(labelStatsMap.containsKey(labelName)) {
                CDLabelStatistics labelStats = labelStatsMap.get(labelName);
                labelStats.setAggScore(labelStats.getAggScore() + weightedLabelScore);
                labelStats.setMaxScore(Math.max(labelStats.getMaxScore(), labelScore));
            }
            else {
                CDLabelStatistics labelStats = new CDLabelStatistics(labelName, weightedLabelScore, labelScore);
                labelStatsMap.put(labelName, labelStats);
            }
        }

        return labelStatsMap;
    }

    /**
     * Choose the label with the highest aggregated values from the neighbors.  (EQ 2 step 2).
     * @return the chosen label
     */
    private String chooseLabel(Map<String, CDLabelStatistics> labelStatsMap) {
        float maxAggScore = Float.NEGATIVE_INFINITY;
        String chosenLabel;

        float epsilon = 0.00001f;

        // chose max score label or random tie break
        List<String> potentialLabels = new ArrayList<String>();

        for(CDLabelStatistics labelStats : labelStatsMap.values()) {
            float aggScore = labelStats.getAggScore();

            if ((aggScore - maxAggScore) > epsilon ) {
                maxAggScore = aggScore;

                potentialLabels.clear();
                potentialLabels.add(labelStats.getLabelName());
            } else if (Math.abs(maxAggScore - aggScore) < epsilon) {
                potentialLabels.add(labelStats.getLabelName());
            }
        }

        // random tie break
        //int labelIndex = (new Random()).nextInt(potentialLabels.size());
        //chosenLabel = potentialLabels.get(labelIndex);

        // for experiment comparasion, chooose the smallest label name for tie break;
        chosenLabel = potentialLabels.get(0);
        for(String label : potentialLabels) {
            if(Long.parseLong(label) < Long.parseLong(chosenLabel)) {
                chosenLabel = label;
            }
        }

        return chosenLabel;
    }

    /**
     * Calculate the attenuated score of the new label (EQ 3)
     * @return the new label score
     */
    private float getChosenLabelScore(Map<String, CDLabelStatistics> labelStatsMap, String chosenLabel, String oldLabel) {
        float chosenLabelMaxScore = labelStatsMap.get(chosenLabel).getMaxScore();
        float delta = 0;
        if (!chosenLabel.equals(oldLabel))
            delta = hopAttenuation;

        return chosenLabelMaxScore - delta;
    }

}