package org.tudelft.graphalytics.mapreducev2.stats;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.tudelft.graphalytics.mapreducev2.common.Edge;
import org.tudelft.graphalytics.mapreducev2.common.UndirectedNode;

public class GatherUndirectedNodeNeighboursInfoMap extends MapReduceBase
        implements Mapper<LongWritable, Text, Text, UndirectedNode>{
    private Text centralId = new Text();
    private Text neighbourId = new Text();
    private UndirectedNode node = new UndirectedNode();

    public void map(LongWritable key, Text value, OutputCollector<Text, UndirectedNode> output, Reporter reporter)
            throws IOException {
        String line = value.toString();

        // build node
        node.readFields(line);

        //broadcast info to all neighbours
        Iterator<Edge> iterator = node.getEdges().iterator();
        while (iterator.hasNext()) {
            neighbourId.set(iterator.next().getDest());
            output.collect(neighbourId, node);
        }

        centralId.set(node.getId());
        output.collect(centralId, node);
    }
}
