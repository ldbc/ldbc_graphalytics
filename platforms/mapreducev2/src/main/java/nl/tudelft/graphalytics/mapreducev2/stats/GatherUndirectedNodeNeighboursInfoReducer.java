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
import java.util.Vector;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import nl.tudelft.graphalytics.mapreducev2.common.UndirectedNode;
import nl.tudelft.graphalytics.mapreducev2.common.UndirectedNodeNeighbourhood;

/**
 * @author Marcin Biczak
 */
public class GatherUndirectedNodeNeighboursInfoReducer extends MapReduceBase
        implements Reducer<Text, UndirectedNode, NullWritable, UndirectedNodeNeighbourhood>{
    private UndirectedNodeNeighbourhood nodeNeighbourhood = new UndirectedNodeNeighbourhood();

    public void reduce(Text key, Iterator<UndirectedNode> values,
                       OutputCollector<NullWritable, UndirectedNodeNeighbourhood> output, Reporter reporter) throws IOException {

        // build central node
        UndirectedNode centralNode = new UndirectedNode();
        centralNode.setId(key.toString());

        Vector<UndirectedNode> centralNodeNeighbourhood = new Vector<UndirectedNode>();
        while (values.hasNext()) {
            UndirectedNode tmp = (values.next()).copy();

            if(tmp.getId().equals(centralNode.getId())) {
                centralNode.setEdges(tmp.getEdges());
            }
            else
                centralNodeNeighbourhood.add(tmp);
        }

        //build node neighbourhood OBJ
        nodeNeighbourhood.setCentralNode(centralNode);
        nodeNeighbourhood.setNodeNeighbourhood(centralNodeNeighbourhood);

        output.collect(null, nodeNeighbourhood);
    }
}
