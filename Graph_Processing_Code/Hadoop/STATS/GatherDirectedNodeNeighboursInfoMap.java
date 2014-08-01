package org.hadoop.test.map.directed;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.hadoop.test.data.directed.DirectedNode;
import org.hadoop.test.data.Edge;

import java.io.IOException;
import java.util.*;

public class GatherDirectedNodeNeighboursInfoMap extends MapReduceBase
        implements Mapper<LongWritable, Text, Text, Text> {
    private Text centralId = new Text();
    private Text neighbourId = new Text();
    private Text emitData = new Text();
    private Text nodeText = new Text();

    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter)
            throws IOException {
        String line = value.toString();
        DirectedNode node = new DirectedNode();
        Vector<Edge> outEdges;

        // build node
        node.readFields(line);

        // prepare chunks for emission
        // chunk pattern "nodeId@[id,id,...,id]"
        outEdges = node.getOutEdges();
        String data = "";
        data += node.getId()+"@";
        if(outEdges.size() == 0) {
            data += ",";
        } else {
            boolean isFirst = true;
            for(Edge edge : outEdges) {
                if(isFirst) {
                    data += edge.getDest();
                    isFirst = false;
                } else
                    data += ","+edge.getDest();
            }
        }
        emitData.set(data);


        /*
              emit only node.outEdges in "nodeId @ [nodeId]" format
         */
        for(String neighbourId : this.gatherNeighboursAsMap(node).keySet()) {
            this.neighbourId.set(neighbourId);
            output.collect(this.neighbourId, emitData);
        }

        // full node data to myself
        centralId.set(node.getId());
        nodeText.set(node.toText());
        output.collect(centralId, nodeText);
    }

    private Map<String, Boolean> gatherNeighboursAsMap(DirectedNode node) {
        Map<String, Boolean> neighboursIdMap = new HashMap<String, Boolean>();

        for(Edge edge : node.getInEdges())
            neighboursIdMap.put(edge.getSrc(), true);

        for(Edge edge : node.getOutEdges())
            neighboursIdMap.put(edge.getDest(), true);

        return neighboursIdMap;
    }
}

