package org.tudelft.graphalytics.yarn.bfs;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.tudelft.graphalytics.yarn.common.Edge;
import org.tudelft.graphalytics.yarn.common.Node;

import java.io.IOException;
import java.util.StringTokenizer;

/*
    GETS:
    - normal filtered node record pattern
    - distance (int wrapped in Text)
    - normal filtered node record pattern + "\t$distance"
    - normal filtered node record pattern + "\t$Tdistance" -> node which should continue propagation
 */
public class UndirectedBFSMap extends MapReduceBase
        implements Mapper<LongWritable, Text, IntWritable, Text> {
    private int srcId;
    private IntWritable id = new IntWritable();
    private IntWritable dst = new IntWritable();
    private final Text zero = new Text("0");
    private Text outputValue = new Text("1");
    private int counter = 0;

    public void map(LongWritable key, Text value, OutputCollector<IntWritable, Text> output, Reporter reporter)
            throws IOException {
        String recordString = value.toString();

        counter++;
        if(counter % 10000 == 0)
            reporter.progress();

        StringTokenizer tokenizer = new StringTokenizer(recordString, "$");
        if(tokenizer.countTokens() == 1) { // node record
            Node node = new Node();
            node.readFields(recordString);
            this.id.set(Integer.parseInt(node.getId()));

            // init BFS by SRC_NODE
            if(this.id.get() == srcId) {
                reporter.incrCounter(UndirectedBFSJob.Node.VISITED, 1);
                for(Edge edge : node.getEdges()) {
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
                reporter.incrCounter(UndirectedBFSJob.Node.VISITED, 1);

                Node node = new Node();
                node.readFields(nodeString);
                this.id.set(Integer.parseInt(node.getId()));
                StringTokenizer dstTokenizer = new StringTokenizer(dst, " ");
                dstTokenizer.nextToken();
                int distance = Integer.parseInt(dstTokenizer.nextToken());
                distance++;

                // propagate bfs
                for(Edge edge : node.getEdges()) {
                    this.dst.set(Integer.parseInt(edge.getDest()));
                    outputValue.set(String.valueOf(distance));
                    output.collect(this.dst, outputValue);
                }

                // pass itself
                outputValue.set(node.toText()+"\t$"+ --distance);
                output.collect(this.id, outputValue);

            } else { // already visited node
                Node node = new Node();
                node.readFields(nodeString);
                this.id.set(Integer.parseInt(node.getId()));
                output.collect(this.id, value);
            }
        }
    }

    public void configure(JobConf job) {
        srcId = Integer.parseInt(job.get(BFSJob.SOURCE_VERTEX_KEY));
    }
}
