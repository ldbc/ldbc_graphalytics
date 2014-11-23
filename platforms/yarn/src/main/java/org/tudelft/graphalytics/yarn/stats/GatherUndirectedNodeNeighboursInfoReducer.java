package org.tudelft.graphalytics.yarn.stats;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.tudelft.graphalytics.yarn.common.Node;
import org.tudelft.graphalytics.yarn.common.NodeNeighbourhood;

public class GatherUndirectedNodeNeighboursInfoReducer extends MapReduceBase
        implements Reducer<Text, Node, NullWritable, NodeNeighbourhood>{
    private NodeNeighbourhood nodeNeighbourhood = new NodeNeighbourhood();

    public void reduce(Text key, Iterator<Node> values,
                       OutputCollector<NullWritable, NodeNeighbourhood> output, Reporter reporter) throws IOException {

        // build central node
        Node centralNode = new Node();
        centralNode.setId(key.toString());

        Vector<Node> centralNodeNeighbourhood = new Vector<Node>();
        while (values.hasNext()) {
            Node tmp = (values.next()).copy();

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
