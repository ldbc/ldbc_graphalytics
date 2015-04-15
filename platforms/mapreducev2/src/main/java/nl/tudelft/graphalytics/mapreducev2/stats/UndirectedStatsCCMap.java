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
package nl.tudelft.graphalytics.mapreducev2.stats;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import nl.tudelft.graphalytics.mapreducev2.common.Edge;
import nl.tudelft.graphalytics.mapreducev2.common.UndirectedNode;
import nl.tudelft.graphalytics.mapreducev2.common.UndirectedNodeNeighbourhood;

/**
 * @author Marcin Biczak
 * @author Tim Hegeman
 */
public class UndirectedStatsCCMap extends MapReduceBase
                       implements Mapper<LongWritable, UndirectedNodeNeighbourhood, Text, DoubleAverage> {
    private final Text aggregateKey = new Text("MEAN");

    public void map(LongWritable key, UndirectedNodeNeighbourhood value, OutputCollector<Text, DoubleAverage> output, Reporter reporter)
            throws IOException {
        double cc = this.nodeCC(value, reporter);

        DoubleAverage ccAverage = new DoubleAverage(cc);
        output.collect(new Text(value.getCentralNode().getId()), ccAverage);
	    output.collect(aggregateKey, ccAverage);
    }

    private double nodeCC(UndirectedNodeNeighbourhood nodeNeighbourhood, Reporter reporter) {
        UndirectedNode centralNode = nodeNeighbourhood.getCentralNode();
        Map<String, Boolean> centralNeighboursIds = this.buildNeighboursMap(centralNode);
        int counter = 0;

        for(UndirectedNode outNode : nodeNeighbourhood.getNodeNeighbourhood()) {
            reporter.progress(); //report to master

            for(Edge edge : outNode.getEdges()) {
                if(centralNeighboursIds.get(edge.getDest()) != null) { //comparing only dst; src is known to be a neighbour
                    counter++;
                }
            }
        }

        int totalDegree = centralNode.getEdges().size();
        double bottom = (totalDegree * (totalDegree - 1));

        if(bottom <= 0)
            return 0;

        return counter/bottom;
    }

    private Map<String, Boolean> buildNeighboursMap(UndirectedNode node) {
        Map<String, Boolean> centralNeighboursIds = new HashMap<String, Boolean>();
        for(Edge edge : node.getEdges()) {
            centralNeighboursIds.put(edge.getDest(), true);
        }

        return centralNeighboursIds;
    }
}


