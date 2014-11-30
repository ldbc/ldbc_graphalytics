package org.tudelft.graphalytics.giraph.stats;

import java.io.IOException;
import java.util.Set;

import org.apache.giraph.conf.ImmutableClassesGiraphConfiguration;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

/**
 * Statistics Algorithm,
 * calculate vertex count, edges count, average degree, average clustering coefficient and out-degree distribution/.
 * Credits: mostly Marcin's code refactored
 * Bug fixed
 * - change avgDegree to edgesNr/verticesNr * 2 as it's for undirected graph
 * - private class attributes are no longer persistent between super-steps, therefore totalDegree need to be recalculated in super-step 2.
 * Changes
 * - add removeVertexRequest(vertex.getId()) in super-step 1 to avoid unnecessary output from non-main vertices.
 */
public class UndirectedStatsComputation extends BasicComputation<LongWritable, Text, NullWritable, Text> {

	private long collectionNode;
	
	@Override
	public void setConf(ImmutableClassesGiraphConfiguration<LongWritable, Text, NullWritable> conf) {
		super.setConf(conf);
		collectionNode = StatsJob.COLLECTION_NODE.get(getConf());
	}
	
    /**
     * 0. send id to outgoing neighours, which allows them to collect incoming edges.
     * 1. send neighboring data (all connected neighbors id) to all neighbors.
     * 2. compute CC and send to main vertex.
     * 3. main vertex computes and outputs avg CC.
     */
    @Override
    public void compute(Vertex<LongWritable, Text, NullWritable> vertex, Iterable<Text> messages) throws IOException {

        if (this.getSuperstep() == 0) {

            //get neighborhood data
            Text outNeighours = Neighourhood.Edges2Text(vertex.getEdges());

            // send my OUT edges to my neighbours
            for (Edge<LongWritable, NullWritable> edge : vertex.getEdges()) {
                sendMessage(edge.getTargetVertexId(), outNeighours);
            }

        } else if (this.getSuperstep() == 1) {

            // calculate node CC
            Set<LongWritable> neighboursIds = Neighourhood.Edges2Set(vertex.getEdges());
            double nodeCC = calcCC(neighboursIds, messages, vertex.getNumEdges());

            Text msg = new Text(String.valueOf(nodeCC));

            // send clustering coefficient to main vertex
            LongWritable mainId = new LongWritable(collectionNode);
            sendMessage(mainId, msg);

            //vertex halts and remove itself (except the main vertex)
            if (!vertex.getId().equals(mainId)) {
                vertex.voteToHalt();
                removeVertexRequest(vertex.getId());
            }
        } else if (this.getSuperstep() == 2 && vertex.getId().equals(new LongWritable(collectionNode))) {
            // executed only by "main" vertex
            // calculate final results
            float avgCC = getAvgCC(messages);

            //vertex halts and stores stats in the value of the main vertex
            Text stats = new Text(String.format("%.2f", avgCC));
            vertex.setValue(stats);
            vertex.voteToHalt();
        } else
            vertex.voteToHalt();
    }

    public double calcCC(Set<LongWritable> uniqueNeighboursIds, Iterable<Text> messages,
            long totalDegree) {


        long connectedEdges = 0;
        for (Text message : messages) {
            String[] destIds = message.toString().split(",");
            for(String destId : destIds) {
                if(destId.length() > 0) {
                    if(uniqueNeighboursIds.contains(new LongWritable(Long.parseLong(destId)))) {
                        connectedEdges++;
                    }
                }
            }
        }

        float totalEdges = (totalDegree * (totalDegree - 1));
        if (totalEdges <= 0) return 0;
        return (double) connectedEdges / totalEdges;
    }

    /**
     * Calculate average clustering coefficient
     */
    private float getAvgCC(Iterable<Text> nodeCCMessages) {
        // calculate average cc and out-degree distribution
        long nodes = 0;
        float nodeCCTotal = 0;
        for (Text message : nodeCCMessages) {
            nodeCCTotal += Double.parseDouble(message.toString());
            nodes++;
        }

        if(nodes == 0) return 0;
        return nodeCCTotal / (float) nodes;
    }

}
