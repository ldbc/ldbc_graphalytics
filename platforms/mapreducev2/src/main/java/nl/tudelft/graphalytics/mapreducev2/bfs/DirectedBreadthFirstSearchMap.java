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
package nl.tudelft.graphalytics.mapreducev2.bfs;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import nl.tudelft.graphalytics.mapreducev2.bfs.BreadthFirstSearchConfiguration.NODE_STATUS;
import nl.tudelft.graphalytics.mapreducev2.common.DirectedNode;
import nl.tudelft.graphalytics.mapreducev2.common.Edge;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * @author Marcin Biczak
 */
public class DirectedBreadthFirstSearchMap extends MapReduceBase
        implements Mapper<LongWritable, Text, Text, Text> {
	
    private String srcId;
    private Text id = new Text();
    private Text dst = new Text();
    private final Text zero = new Text("0");
    private Text outputValue = new Text("1");
    
    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter)
            throws IOException {
        String recordString = value.toString();
        StringTokenizer tokenizer = new StringTokenizer(recordString, "$");
        if(tokenizer.countTokens() == 1) { // node record
            DirectedNode node = new DirectedNode();
            node.readFields(recordString);
            this.id.set(node.getId());

            // init BFS by SRC_NODE
            if(node.getId().equals(srcId)) {
                reporter.incrCounter(NODE_STATUS.VISITED, 1);
                for(Edge edge : node.getOutEdges()) {
                    dst.set(edge.getDest());
                    output.collect(this.dst, outputValue);
                }
                output.collect(this.id, zero);
            }

            output.collect(this.id, node.toText());
        } else { // visited node record
            // check if node should propagate bfs
            String nodeString = tokenizer.nextToken();
            String dst = tokenizer.nextToken();
            if(dst.startsWith("T")) { //propagate bfs msg
                // mark that iteration should continue, since nodes are still propagating bfs msgs
                reporter.incrCounter(NODE_STATUS.VISITED, 1);

                DirectedNode node = new DirectedNode();
                node.readFields(nodeString);

                this.id.set(node.getId());
                StringTokenizer dstTokenizer = new StringTokenizer(dst, " ");
                dstTokenizer.nextToken();
                int distance = Integer.parseInt(dstTokenizer.nextToken());
                distance++;

                // propagate bfs
                for(Edge edge : node.getOutEdges()) {
                    this.dst.set(edge.getDest());
                    outputValue.set(String.valueOf(distance));
                    output.collect(this.dst, outputValue);
                }

                // pass itself
                outputValue.set(node.toText()+"\t$"+ --distance);
                output.collect(this.id, outputValue);

            } else { // already visited node
                DirectedNode node = new DirectedNode();
                node.readFields(nodeString);
                this.id.set(node.getId());
                output.collect(this.id, value);
            }
        }
    }

    public void configure(JobConf job) {
        srcId = job.get(BreadthFirstSearchConfiguration.SOURCE_VERTEX_KEY);
    }
}

