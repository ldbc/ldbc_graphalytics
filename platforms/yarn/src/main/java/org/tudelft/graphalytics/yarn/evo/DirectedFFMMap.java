package org.tudelft.graphalytics.yarn.evo;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.tudelft.graphalytics.yarn.common.DirectedNode;
import org.tudelft.graphalytics.yarn.common.Edge;

import java.io.IOException;
import java.util.*;

public class DirectedFFMMap extends MapReduceBase implements Mapper<LongWritable, Text, LongWritable, Text> {
    private LongWritable oKey = new LongWritable();
    private Text  oVal = new Text();
    private boolean isFirst = true; // used in creating new vertices
    private boolean isInit = false;
    private int taskID = 0;
    private int newVerticesPerSlot = 0;
    private long maxID = 0;
    private List<LongWritable> newVertices = new ArrayList<LongWritable>();
    private Map<LongWritable, List<LongWritable>> ambassadors;   // k - ambassadors, v - newVertices

    @Override
    public void configure(JobConf conf) {
        TaskAttemptID attempt = TaskAttemptID.forName(conf.get("mapred.task.id"));
        this.taskID = attempt.getTaskID().getId(); // todo verify
        this.newVerticesPerSlot = conf.getInt(FFMUtils.NEW_VERTICES_NR, -1);
        this.maxID = conf.getLong(FFMUtils.MAX_ID, -1);
        this.isFirst = conf.getBoolean(FFMUtils.IS_INIT, false);
        this.isInit = this.isFirst;

        if(this.isInit)
            this.ambassadors = new HashMap<LongWritable, List<LongWritable>>();
        else
            this.ambassadors = FFMUtils.verticesIdsString2Map(conf.get(FFMUtils.CURRENT_AMBASSADORS));
    }

    public void map(LongWritable key, Text value, OutputCollector<LongWritable, Text> output, Reporter reporter)
            throws IOException {
        DirectedNode node = new DirectedNode();
        node.readFields(value.toString());

        if(this.isFirst) { // INIT_JOB
            this.isFirst = false;
            // create N new vertices
            for(int i=0; i<this.newVerticesPerSlot; i++) {
                long newID = this.taskID * this.newVerticesPerSlot + i + this.maxID;
                DirectedNode newVertex = new DirectedNode(String.valueOf(newID), new Vector<Edge>(), new Vector<Edge>());

                this.newVertices.add(new LongWritable(newID)); // same as in Giraph can connect only to worker ambassadors

                oKey.set(newID);
                oVal.set(newVertex.toText());
                output.collect(oKey, oVal);
            }
        } else if(this.ambassadors.containsKey(new LongWritable(Long.parseLong(node.getId())))) { //update vertex
            Vector<Edge> edges = node.getInEdges();

            for(LongWritable id : this.ambassadors.get(new LongWritable(Long.parseLong(node.getId()))))
                edges.add(new Edge(node.getId(), id.toString()));
            node.setInEdges(edges);
        } else { // check if potential ambassador n send to new vertex
            Set<LongWritable> edges = new HashSet<LongWritable>();
            for(Edge out : node.getOutEdges())
                edges.add(new LongWritable(Long.parseLong(out.getDest())));
            for(Edge in : node.getInEdges())
                edges.add(new LongWritable(Long.parseLong(in.getSrc())));

            for(LongWritable neighbour : edges) {
                if(ambassadors.containsKey(neighbour)) {
                    // send my id to new vertices
                    List<LongWritable> newVertices = this.ambassadors.get(neighbour);
                    for(LongWritable id : newVertices)
                        output.collect(id, new Text(node.getId()));
                }
            }
        }

        // Init step -> pass all worker verticesIDs to all newVertices from this worker
        if(this.isInit) {
            oVal.set(node.getId());
            for(LongWritable id : this.newVertices) {
                oKey.set(id.get());
                output.collect(oKey, oVal);
            }
        }

        // pass node
        oKey.set(Long.parseLong(node.getId()));
        oVal.set(node.toText());
        output.collect(oKey, oVal);
    }
}

