package org.test.giraph.data;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.VIntWritable;

import java.util.Vector;

/*
    Class stores only dst of edges.
 */
public class Neighbourhood {
    private VIntWritable id;
    private Vector<VIntWritable> outEdgesDst;

    public Neighbourhood(VIntWritable id) {
        this.id = id;
    }

    public Neighbourhood(VIntWritable id, Vector<VIntWritable> outEdgesDst) {
        this.id = id;
        this.outEdgesDst = outEdgesDst;
    }

    public VIntWritable getId() { return id; }
    public void setId(VIntWritable id) { this.id = id; }

    public Vector<VIntWritable> getOutEdgesDst() { return outEdgesDst; }
    public void setOutEdgesDst(Vector<VIntWritable> outEdgesDst) { this.outEdgesDst = outEdgesDst;}
}
