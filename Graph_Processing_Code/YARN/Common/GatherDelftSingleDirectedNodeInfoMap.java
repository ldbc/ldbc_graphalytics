package org.hadoop.test.map.directed;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.hadoop.test.data.Edge;

import java.io.IOException;
import java.util.StringTokenizer;

public class GatherDelftSingleDirectedNodeInfoMap extends MapReduceBase
        implements Mapper<LongWritable, Text, Text, Text> {
    private Text nodeA = new Text();
    private Text nodeB = new Text();

    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter)
            throws IOException {
        String line = value.toString();

        StringTokenizer tokenizer = new StringTokenizer(line, " \t");
        String firstToken = tokenizer.nextToken();
        if(firstToken.startsWith("#") || !Character.isDigit(firstToken.charAt(0)))
                return;

        if(tokenizer.countTokens() == 5) { //compensate for the token already taken while checking for meta data
            tokenizer.nextToken();
            nodeA.set(tokenizer.nextToken());

            tokenizer.nextToken();
            nodeB.set(tokenizer.nextToken());
        }
        else {
            throw new IOException("Invalid input. Didn't get exactly 6 tokens (delft format).");
        }

        output.collect(nodeA, nodeB);
        output.collect(nodeB, nodeA);
    }
}