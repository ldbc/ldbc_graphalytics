package org.tudelft.graphalytics.yarn.bfs;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tudelft.graphalytics.yarn.common.DirectedNode;
import org.tudelft.graphalytics.yarn.common.Edge;

import java.io.IOException;
import java.util.StringTokenizer;

public class DirectedBFSMap extends MapReduceBase
        implements Mapper<LongWritable, Text, IntWritable, Text> {
	private static final Logger log = LogManager.getLogger(DirectedBFSMap.class);
	
    private int srcId;
    private IntWritable id = new IntWritable();
    private IntWritable dst = new IntWritable();
    private final Text zero = new Text("0");
    private Text outputValue = new Text("1");
    
    public void map(LongWritable key, Text value, OutputCollector<IntWritable, Text> output, Reporter reporter)
            throws IOException {
        String recordString = value.toString();
        StringTokenizer tokenizer = new StringTokenizer(recordString, "$");
        if(tokenizer.countTokens() == 1) { // node record
            DirectedNode node = new DirectedNode();
            node.readFields(recordString);
            this.id.set(Integer.parseInt(node.getId()));

            // init BFS by SRC_NODE
            if(this.id.get() == srcId) {
                reporter.incrCounter(DirectedBFSJob.Node.VISITED, 1);
                for(Edge edge : node.getOutEdges()) {
                    dst.set(Integer.parseInt(edge.getDest()));
                    output.collect(this.dst, outputValue);
                }
                output.collect(this.id, zero);
            }

            output.collect(this.id, node.toText());
        } else { // visited node record
            // check if node should propagate bfs
            String nodeString = tokenizer.nextToken();
            String dst = tokenizer.nextToken();
            if(dst.startsWith("T")) { //propagate bfs msg
                // mark that iteration should continue, since nodes are still propagating bfs msgs
                reporter.incrCounter(DirectedBFSJob.Node.VISITED, 1);

                DirectedNode node = new DirectedNode();
                node.readFields(nodeString);

                this.id.set(Integer.parseInt(node.getId()));
                StringTokenizer dstTokenizer = new StringTokenizer(dst, " ");
                dstTokenizer.nextToken();
                int distance = Integer.parseInt(dstTokenizer.nextToken());
                distance++;

                // propagate bfs
                for(Edge edge : node.getOutEdges()) {
                    this.dst.set(Integer.parseInt(edge.getDest()));
                    outputValue.set(String.valueOf(distance));
                    output.collect(this.dst, outputValue);
                }

                // pass itself
                outputValue.set(node.toText()+"\t$"+ --distance);
                output.collect(this.id, outputValue);

            } else { // already visited node
                DirectedNode node = new DirectedNode();
                node.readFields(nodeString);
                this.id.set(Integer.parseInt(node.getId()));
                output.collect(this.id, value);
            }
        }
    }

    public void configure(JobConf job) {
        srcId = Integer.parseInt(job.get(BFSJob.SRC_ID_KEY));
    }
}

