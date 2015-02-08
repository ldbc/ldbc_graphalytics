/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.tudelft.graphalytics.giraph.conn;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;

import java.io.IOException;

/**
 * Undirected Connected Component algorithm.
 * Credits: mostly from Giraph example
 * https://github.com/apache/giraph/blob/trunk/giraph-examples/src/main/java/org/apache/giraph/examples/ConnectedComponentsComputation.java
 * <p/>
 * Bug Fixed (Wing)
 * - The vertex value was never initialized with the vertex id
 *
 * @author Wing Ngai
 */
public class UndirectedConnectedComponentsComputation extends BasicComputation<LongWritable, LongWritable, NullWritable, LongWritable> {
    /**
     * Propagates the smallest vertex id to all neighbors. Will always choose to
     * halt and only reactivate if a smaller id has been sent to it.
     */
    @Override
    public void compute(Vertex<LongWritable, LongWritable, NullWritable> vertex, Iterable<LongWritable> messages) throws IOException {
        long currentComponent = vertex.getValue().get();

        // First superstep is special, because we can simply look at the neighbors
        if (getSuperstep() == 0) {
            for (Edge<LongWritable, NullWritable> edge : vertex.getEdges()) {
                long neighbor = edge.getTargetVertexId().get();
                if (neighbor < currentComponent) {
                    currentComponent = neighbor;
                }
            }
            // only need to send value if it is not the own id
            if (currentComponent != vertex.getValue().get()) {
                vertex.setValue(new LongWritable(currentComponent));
                for (Edge<LongWritable, NullWritable> edge : vertex.getEdges()) {
                    LongWritable neighbor = edge.getTargetVertexId();
                    if (neighbor.get() > currentComponent) {
                        sendMessage(neighbor, vertex.getValue());
                    }
                }
            }

            vertex.voteToHalt();
            return;
        }

        boolean changed = false;
        // did we get a smaller id ?
        for (LongWritable message : messages) {
            long candidateComponent = message.get();
            if (candidateComponent < currentComponent) {
                currentComponent = candidateComponent;
                changed = true;
            }
        }

        // propagate new component id to the neighbors
        if (changed) {
            vertex.setValue(new LongWritable(currentComponent));
            sendMessageToAllEdges(vertex, vertex.getValue());
        }
        vertex.voteToHalt();
    }
}