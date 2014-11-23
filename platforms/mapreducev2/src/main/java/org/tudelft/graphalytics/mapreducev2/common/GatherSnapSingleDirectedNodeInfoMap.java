package org.tudelft.graphalytics.mapreducev2.common;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.StringTokenizer;

public class GatherSnapSingleDirectedNodeInfoMap extends MapReduceBase
        implements Mapper<LongWritable, Text, Text, Text> {
    private Text nodeA = new Text();
    private Text nodeB = new Text();
    private Text edgeText = new Text();

    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter)
            throws IOException {
        String line = value.toString();
        Edge edge = new Edge();

        if(line.charAt(0) == '#')
            return;

        StringTokenizer tokenizer = new StringTokenizer(line, " \t\n\r\f,.:;?![]'");
        if(tokenizer.countTokens() == 2) {
            edge.setSrc(tokenizer.nextToken());
            edge.setDest(tokenizer.nextToken());

        }else throw new IOException("Error while reading. File format not supported (SNAP format).");

        nodeA.set(edge.getSrc());
        nodeB.set(edge.getDest());
        edgeText.set(edge.toString());

        output.collect(nodeA, edgeText);
        output.collect(nodeB, edgeText);
    }
}


