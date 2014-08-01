package org.hadoop.test.reduce.directed;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.hadoop.test.data.directed.DirectedNode;
import org.hadoop.test.data.Edge;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

public class GatherSingleDirectedNodeInfoReducer extends MapReduceBase
        implements Reducer<Text, Text, NullWritable, Text> {
    private Text nodeText = new Text();

    public void reduce(Text key, Iterator<Text> values,
                       OutputCollector<NullWritable, Text> output, Reporter reporter) throws IOException {
        DirectedNode node = new DirectedNode();
        Vector<Edge> nodeInEdges = new Vector<Edge>();
        Vector<Edge> nodeOutEdges = new Vector<Edge>();
        // id
        node.setId(key.toString());
        while(values.hasNext()) {
            Text tmpEdge = values.next();
            StringTokenizer chunkTokenizer = new StringTokenizer(tmpEdge.toString(), "|");
            while (chunkTokenizer.hasMoreTokens()) {
                StringTokenizer tokenizer = new StringTokenizer(chunkTokenizer.nextToken(), ",");
                while (tokenizer.hasMoreTokens()) {
                    String edgeSrc = tokenizer.nextToken();
                    String edgeDst = tokenizer.nextToken();
                    Edge nodeEdge = new Edge(edgeSrc, edgeDst);
                    if(nodeEdge.getSrc().equals(node.getId()))
                        nodeOutEdges.add(nodeEdge);
                    else
                        nodeInEdges.add(nodeEdge);
                }
            }
        }

        node.setInEdges(nodeInEdges);
        node.setOutEdges(nodeOutEdges);
        nodeText.set(node.toText());
        output.collect(null, nodeText);
    }
}
