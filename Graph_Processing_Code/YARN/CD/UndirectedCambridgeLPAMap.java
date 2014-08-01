package org.hadoop.test.map.community;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.hadoop.test.data.Edge;
import org.hadoop.test.data.undirected.Node;

import java.io.IOException;
import java.util.StringTokenizer;

public class UndirectedCambridgeLPAMap extends MapReduceBase
        implements Mapper<LongWritable, Text, VIntWritable, Text> {
    private VIntWritable oKey = new VIntWritable();
    private Text oVal = new Text();

    public void map(LongWritable key, Text value, OutputCollector<VIntWritable, Text> output, Reporter reporter)
            throws IOException {
        String record = value.toString();
        Node node = new Node();
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

        // send to neighbours
        oVal.set(label+"|"+labelScore+"|"+node.getEdges().size());
        for(Edge edge : node.getEdges()) {
            oKey.set(Integer.parseInt(edge.getDest()));
            output.collect(oKey, oVal);
        }

        // propagate vertex data
        oKey.set(Integer.parseInt(node.getId()));
        oVal.set(node.toText()+"$"+label);
        output.collect(oKey, oVal);
    }
}
