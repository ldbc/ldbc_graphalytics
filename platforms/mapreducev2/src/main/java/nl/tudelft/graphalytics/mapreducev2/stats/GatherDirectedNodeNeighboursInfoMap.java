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

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import nl.tudelft.graphalytics.mapreducev2.common.DirectedNode;
import nl.tudelft.graphalytics.mapreducev2.common.Edge;

import java.io.IOException;
import java.util.*;

/**
 * @author Marcin Biczak
 */
public class GatherDirectedNodeNeighboursInfoMap extends MapReduceBase
        implements Mapper<LongWritable, Text, Text, Text> {
    private Text centralId = new Text();
    private Text neighbourId = new Text();
    private Text emitData = new Text();
    private Text nodeText = new Text();

    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter)
            throws IOException {
        String line = value.toString();
        DirectedNode node = new DirectedNode();
        Vector<Edge> outEdges;

        // build node
        node.readFields(line);

        // prepare chunks for emission
        // chunk pattern "nodeId@[id,id,...,id]"
        outEdges = node.getOutEdges();
        StringBuilder data = new StringBuilder();
        data.append(node.getId()).append("@");
        if(outEdges.size() == 0) {
            data.append(",");
        } else {
            boolean isFirst = true;
            for(Edge edge : outEdges) {
                if(isFirst) {
                    data.append(edge.getDest());
                    isFirst = false;
                } else
                    data.append(",").append(edge.getDest());
            }
        }
        emitData.set(data.toString());


        /*
              emit only node.outEdges in "nodeId @ [nodeId]" format
         */
        for(String neighbourId : this.gatherNeighboursAsMap(node).keySet()) {
            this.neighbourId.set(neighbourId);
            output.collect(this.neighbourId, emitData);
        }

        // full node data to myself
        centralId.set(node.getId());
        nodeText.set(node.toText());
        output.collect(centralId, nodeText);
    }

    private Map<String, Boolean> gatherNeighboursAsMap(DirectedNode node) {
        Map<String, Boolean> neighboursIdMap = new HashMap<String, Boolean>();

        for(Edge edge : node.getInEdges())
            neighboursIdMap.put(edge.getSrc(), true);

        for(Edge edge : node.getOutEdges())
            neighboursIdMap.put(edge.getDest(), true);

        return neighboursIdMap;
    }
}

