package org.hadoop.test.map.undirected;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.StringTokenizer;

public class GatherSnapSingleUndirectedNodeInfoMap extends MapReduceBase
        implements Mapper<LongWritable, Text, Text, Text> {
    private Text nodeA = new Text();
    private Text nodeB = new Text();
    private int counter = 0;

    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter)
            throws IOException {
        String line = value.toString();
        counter++;

        if(line.charAt(0) == '#')
            return;

        StringTokenizer tokenizer = new StringTokenizer(line, " \t\n\r\f,.:;?![]'");
        if(tokenizer.countTokens() == 2) {
            nodeA.set(tokenizer.nextToken());
            nodeB.set(tokenizer.nextToken());

        }else throw new IOException("Error while reading. File format not supported (SNAP format).");

        output.collect(nodeA, nodeB);
        output.collect(nodeB, nodeA);

        if(counter % 10000 == 0)
            reporter.progress();
    }
}

