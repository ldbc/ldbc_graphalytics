package org.tudelft.graphalytics.yarn.stats;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.tudelft.graphalytics.yarn.common.DirectedNode;
import org.tudelft.graphalytics.yarn.common.DirectedNodeNeighbourhood;
import org.tudelft.graphalytics.yarn.common.Edge;
import org.tudelft.graphalytics.yarn.common.OutNode;
import org.tudelft.graphalytics.yarn.common.StatsCCContainer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DirectedStatsCCMap extends MapReduceBase
                       implements Mapper<LongWritable, DirectedNodeNeighbourhood, IntWritable, StatsCCContainer> {
    private StatsCCContainer container = new StatsCCContainer();
    private final IntWritable fakeKey = new IntWritable(0);

    public void map(LongWritable key, DirectedNodeNeighbourhood value, OutputCollector<IntWritable, StatsCCContainer> output, Reporter reporter)
            throws IOException {
        double cc = this.nodeCC(value, reporter);

        this.buildStatsContainer(cc, value.getCentralNode(), container);
        output.collect(fakeKey, container);
    }

    private StatsCCContainer buildStatsContainer(double cc, DirectedNode node, StatsCCContainer container) {
        container.setNodesNr(1);
        container.setEdgesNr(node.getOutEdges().size());
        container.setCc(cc);
        return container;
    }

    private double nodeCC(DirectedNodeNeighbourhood nodeNeighbourhood, Reporter reporter) {
        DirectedNode centralNode = nodeNeighbourhood.getCentralNode();
        Map<String, Boolean> centralNeighboursIds = this.buildNeighboursMap(centralNode);
        int counter = 0;

        for(OutNode outNode : nodeNeighbourhood.getDirectedNodeNeighbourhood()) {
            reporter.progress(); //report to master

            for(Edge edge : outNode.getOutEdges()) {
                if(centralNeighboursIds.get(edge.getDest()) != null) { //comparing only dst; src is known to be a neighbour
                    counter++;
                }
            }
        }

        int totalDegree = centralNode.getInEdges().size() + centralNode.getOutEdges().size();
        double bottom = (totalDegree * (totalDegree - 1));

        if(bottom <= 0)
            return 0;

        return counter/bottom;
    }

    private Map<String, Boolean> buildNeighboursMap(DirectedNode node) {
        Map<String, Boolean> centralNeighboursIds = new HashMap<String, Boolean>();
        // IN
        for(Edge edge : node.getInEdges()) {
            centralNeighboursIds.put(edge.getSrc(), true);
        }

        // OUT
        for(Edge edge : node.getOutEdges()) {
            centralNeighboursIds.put(edge.getDest(), true);
        }

        return centralNeighboursIds;
    }
}


