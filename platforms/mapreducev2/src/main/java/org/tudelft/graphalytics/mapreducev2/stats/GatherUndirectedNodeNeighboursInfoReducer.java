package org.tudelft.graphalytics.mapreducev2.stats;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.tudelft.graphalytics.mapreducev2.common.UndirectedNode;
import org.tudelft.graphalytics.mapreducev2.common.UndirectedNodeNeighbourhood;

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
