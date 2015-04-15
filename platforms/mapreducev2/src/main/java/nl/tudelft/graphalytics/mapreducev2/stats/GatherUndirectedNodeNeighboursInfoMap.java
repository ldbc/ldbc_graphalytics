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
import java.util.Iterator;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import nl.tudelft.graphalytics.mapreducev2.common.Edge;
import nl.tudelft.graphalytics.mapreducev2.common.UndirectedNode;

/**
 * @author Marcin Biczak
 */
public class GatherUndirectedNodeNeighboursInfoMap extends MapReduceBase
        implements Mapper<LongWritable, Text, Text, UndirectedNode>{
    private Text centralId = new Text();
    private Text neighbourId = new Text();
    private UndirectedNode node = new UndirectedNode();

    public void map(LongWritable key, Text value, OutputCollector<Text, UndirectedNode> output, Reporter reporter)
            throws IOException {
        String line = value.toString();

        // build node
        node.readFields(line);

        //broadcast info to all neighbours
        Iterator<Edge> iterator = node.getEdges().iterator();
        while (iterator.hasNext()) {
            neighbourId.set(iterator.next().getDest());
            output.collect(neighbourId, node);
        }

        centralId.set(node.getId());
        output.collect(centralId, node);
    }
}
