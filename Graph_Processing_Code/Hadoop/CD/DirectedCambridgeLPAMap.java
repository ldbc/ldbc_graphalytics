package org.hadoop.test.map.community;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.hadoop.test.data.Edge;
import org.hadoop.test.data.directed.DirectedNode;
import org.hadoop.test.data.undirected.Node;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class DirectedCambridgeLPAMap extends MapReduceBase
        implements Mapper<LongWritable, Text, VIntWritable, Text> {
    private VIntWritable oKey = new VIntWritable();
    private Text oVal = new Text();

    public void map(LongWritable key, Text value, OutputCollector<VIntWritable, Text> output, Reporter reporter)
            throws IOException {
        String record = value.toString();
        DirectedNode node = new DirectedNode();
        String label;
        String labelScore; // init label score

        /* read vertex data */
        // init iteration
        if(record.indexOf("$") == -1) {
            node.readFields(record);
            label = new String(node.getId());
            labelScore = new String("1");
        }
        // N iteration
        else {
            StringTokenizer tokenizer = new StringTokenizer(record, "$");
            String nodeData = tokenizer.nextToken();
            node.readFields(nodeData);

            StringTokenizer labelTokenizer = new StringTokenizer(tokenizer.nextToken(), "|");
            label = labelTokenizer.nextToken();
            labelScore = labelTokenizer.nextToken();
        }

        oVal.set(label+"|"+labelScore+"|"+(node.getInEdges().size() + node.getOutEdges().size()));

        Set<String> uniqueNeighbours = new HashSet<String>();
        for(Edge edge : node.getOutEdges())
            uniqueNeighbours.add(edge.getDest());

        // send to IN neighbours
        for(Edge edge : node.getInEdges())
            uniqueNeighbours.add(edge.getSrc());

        for(String dst : uniqueNeighbours) {
            oKey.set(Integer.parseInt(dst));
            output.collect(oKey, oVal);
        }

        // propagate vertex data
        oKey.set(Integer.parseInt(node.getId()));
        oVal.set(node.toText()+"$"+label);
        output.collect(oKey, oVal);
    }
}
