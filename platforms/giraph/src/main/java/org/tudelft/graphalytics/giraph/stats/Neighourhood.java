package org.tudelft.graphalytics.giraph.stats;

import org.apache.giraph.edge.Edge;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

import java.util.HashSet;
import java.util.Set;

public class Neighourhood {


    public static Set<LongWritable> Text2Set(Text neighboursText) {
        Set<LongWritable> neighboursIds = new HashSet<LongWritable>();

        String[] ids = neighboursText.toString().split(",");
        for(String id : ids) {
            if(id.length() > 0) {
                neighboursIds.add(new LongWritable(Long.parseLong(id)));
            }
        }
        return neighboursIds;
    }

    public static Set<LongWritable> Edges2Set(Iterable<Edge<LongWritable, NullWritable>> outEdges) {
        Set<LongWritable> neighboursIds = new HashSet<LongWritable>();

        for (Edge<LongWritable, NullWritable> edge : outEdges) {
            neighboursIds.add(new LongWritable(edge.getTargetVertexId().get()));
        }
        return neighboursIds;
    }

    public static Text Edges2Text(Iterable<Edge<LongWritable, NullWritable>> edges) {

        Text msg = new Text("");
        for (Edge<LongWritable, NullWritable> edge : edges) {
            byte[] neighbour = (String.valueOf(edge.getTargetVertexId())).getBytes();
            msg.append(neighbour, 0, neighbour.length);
            msg.append(",".getBytes(), 0, 1);
        }
        return msg;
    }


    public static Text Set2Text(Set<LongWritable> neighbours) {

        Text msg = new Text("");
        for (LongWritable neighour : neighbours) {
            byte[] outNeighbour = (String.valueOf(neighour.get())).getBytes();
            msg.append(outNeighbour, 0, outNeighbour.length);
            msg.append(",".getBytes(), 0, 1);
        }
        return msg;
    }

    public static Set<LongWritable> UniqueSet(Iterable<Edge<LongWritable, NullWritable>> outEdges, Iterable<Text> inEdgesMessages) {
        Set<LongWritable> neighboursIds = new HashSet<LongWritable>();

        for (Edge<LongWritable, NullWritable> edge : outEdges) {
            neighboursIds.add(new LongWritable(edge.getTargetVertexId().get()));
        }
        for(Text inEdgeMessage : inEdgesMessages) {
            neighboursIds.add(new LongWritable(Long.parseLong(inEdgeMessage.toString())));
        }
        return neighboursIds;
    }
}
