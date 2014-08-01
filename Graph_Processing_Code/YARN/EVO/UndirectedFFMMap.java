package org.hadoop.test.map.undirected;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapreduce.TaskID;
import org.hadoop.test.data.Edge;
import org.hadoop.test.data.undirected.Node;
import org.hadoop.test.jobs.tasks.ffm.FFMUtils;

import java.io.IOException;
import java.util.*;

/**
 */
public class UndirectedFFMMap extends MapReduceBase implements Mapper<LongWritable, Text, VIntWritable, Text> {
    private VIntWritable oKey = new VIntWritable();
    private Text  oVal = new Text();
    private boolean isFirst = true; // used in creating new vertices
    private boolean isInit = false;
    private int taskID = 0;
    private int newVerticesPerSlot = 0;
    private int maxID = 0;
    private List<VIntWritable> newVertices = new ArrayList<VIntWritable>();
    private Map<VIntWritable, List<VIntWritable>> ambassadors;   // k - ambassadors, v - newVertices

    @Override
    public void configure(JobConf conf) {
        TaskAttemptID attempt = TaskAttemptID.forName(conf.get("mapred.task.id"));
        this.taskID = attempt.getTaskID().getId();
        this.newVerticesPerSlot = conf.getInt(FFMUtils.NEW_VERTICES_NR, -1);
        this.maxID = conf.getInt(FFMUtils.MAX_ID, -1);
        this.isFirst = conf.getBoolean(FFMUtils.IS_INIT, false);
        this.isInit = this.isFirst;

        if(this.isInit)
            this.ambassadors = new HashMap<VIntWritable, List<VIntWritable>>();
        else
            this.ambassadors = FFMUtils.verticesIdsString2Map(conf.get(FFMUtils.CURRENT_AMBASSADORS));
    }

    public void map(LongWritable key, Text value, OutputCollector<VIntWritable, Text> output, Reporter reporter)
            throws IOException {
        Node node = new Node();
        node.readFields(value.toString());

        if(this.isFirst) { // INIT_JOB
            this.isFirst = false;
            // create N new vertices
            for(int i=0; i<this.newVerticesPerSlot; i++) {
                int newID = this.taskID * this.newVerticesPerSlot + i + this.maxID;
                Node newVertex = new Node(String.valueOf(newID), new Vector<Edge>());

                this.newVertices.add(new VIntWritable(newID)); // same as in Giraph can connect only to worker ambassadors

                oKey.set(newID);
                oVal.set(newVertex.toText());
                output.collect(oKey, oVal);
            }
        } else if(this.ambassadors.containsKey(new VIntWritable(Integer.parseInt(node.getId())))) { //update vertex
            Vector<Edge> edges = node.getEdges();

            for(VIntWritable id : this.ambassadors.get(new VIntWritable(Integer.parseInt(node.getId()))))
                edges.add(new Edge(node.getId(), id.toString()));
            node.setEdges(edges);
        } else { // check if potential ambassador n send to new vertex
            for(Edge edge : node.getEdges()) {
                int neighbour = Integer.parseInt(edge.getDest());
                if(ambassadors.containsKey(new VIntWritable(neighbour))) {
                    // send my id to new vertices
                    List<VIntWritable> newVertices = this.ambassadors.get(new VIntWritable(neighbour));
                    for(VIntWritable id : newVertices)
                        output.collect(id, new Text(node.getId()));
                }
            }
        }

        // Init step -> pass all worker verticesIDs to all newVertices from this worker
        if(this.isInit) {
            oVal.set(node.getId());
            for(VIntWritable id : this.newVertices) {
                oKey.set(id.get());
                output.collect(oKey, oVal);
            }
        }

        // pass node
        oKey.set(Integer.parseInt(node.getId()));
        oVal.set(node.toText());
        output.collect(oKey, oVal);
    }
}
