package org.tudelft.graphalytics.mapreducev2.common;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

public class GatherSingleUndirectedNodeInfoReducer extends MapReduceBase
        implements Reducer<Text, Text, NullWritable, Text> {
    private Text nodeText = new Text();
    private int counter = 0;

    public void reduce(Text key, Iterator<Text> values,
                       OutputCollector<NullWritable, Text> output, Reporter reporter) throws IOException {
        counter ++;
        Node node = new Node();
        Vector<Edge> nodesEdges = new Vector<Edge>();
        node.setId(key.toString());
        while(values.hasNext()) {
            Text edgesChunk = values.next();
            StringTokenizer tokenizer = new StringTokenizer(edgesChunk.toString(), " \t\n\r\f,.:;?![]'");
            while (tokenizer.hasMoreTokens()) {
                String edgeDst = tokenizer.nextToken();
                Edge tmpEdge = new Edge(node.getId(), edgeDst);
                nodesEdges.add(tmpEdge);
            }
        }

        node.setEdges(nodesEdges);
        nodeText.set(node.toText());
        output.collect(null, nodeText);

        if(counter % 10000 == 0)
            reporter.progress();
    }
}
