package org.tudelft.graphalytics.yarn.stats;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.tudelft.graphalytics.yarn.common.Edge;
import org.tudelft.graphalytics.yarn.common.Node;
import org.tudelft.graphalytics.yarn.common.NodeNeighbourhood;
import org.tudelft.graphalytics.yarn.common.StatsCCContainer;

public class UndirectedStatsCCMap extends MapReduceBase
                       implements Mapper<LongWritable, NodeNeighbourhood, IntWritable, StatsCCContainer> {
    private StatsCCContainer container = new StatsCCContainer();
    private final IntWritable fakeKey = new IntWritable(0);

    public void map(LongWritable key, NodeNeighbourhood value, OutputCollector<IntWritable, StatsCCContainer> output, Reporter reporter)
            throws IOException {

        double cc = this.nodeCC(value, reporter);

        this.buildStatsContainer(cc, value.getCentralNode(), container);
        output.collect(fakeKey, container);
    }

    private StatsCCContainer buildStatsContainer(double cc, Node node, StatsCCContainer container) {
        container.setNodesNr(1);
        container.setEdgesNr(node.getEdges().size());
        container.setCc(cc);
        return container;
    }

    private double nodeCC(NodeNeighbourhood nodeNeighbourhood, Reporter reporter) {
        Node centralNode = nodeNeighbourhood.getCentralNode();
        Map<String, Boolean> centralNeighboursIds = this.buildNeighboursMap(centralNode);
        int counter = 0;

        for(Node outNode : nodeNeighbourhood.getNodeNeighbourhood()) {
            reporter.progress(); //report to master

            for(Edge edge : outNode.getEdges()) {
                if(centralNeighboursIds.get(edge.getDest()) != null) { //comparing only dst; src is known to be a neighbour
                    counter++;
                }
            }
        }

        int totalDegree = centralNode.getEdges().size();
        double bottom = (totalDegree * (totalDegree - 1));

        if(bottom <= 0)
            return 0;

        return counter/bottom;
    }

    private Map<String, Boolean> buildNeighboursMap(Node node) {
        Map<String, Boolean> centralNeighboursIds = new HashMap<String, Boolean>();
        for(Edge edge : node.getEdges()) {
            centralNeighboursIds.put(edge.getDest(), true);
        }

        return centralNeighboursIds;
    }
}


