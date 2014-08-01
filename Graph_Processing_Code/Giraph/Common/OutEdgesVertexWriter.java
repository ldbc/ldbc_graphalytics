package org.test.giraph.utils.writers.generic;

import org.apache.giraph.graph.BasicVertex;
import org.apache.giraph.lib.TextVertexOutputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.mapreduce.RecordWriter;

import java.io.IOException;
import java.util.Iterator;

/**
 Dumps vertex graph data. ONLY ID and OutEdges
 */
public class OutEdgesVertexWriter extends TextVertexOutputFormat.TextVertexWriter<VIntWritable, Text, VIntWritable> {
    /**
     * Vertex writer with the internal line writer.
     *
     * @param lineRecordWriter Wil actually be written to.
     */
    public OutEdgesVertexWriter(
            RecordWriter<Text, Text> lineRecordWriter) {
        super(lineRecordWriter);
    }

    public void writeVertex(BasicVertex<VIntWritable, Text, VIntWritable, ?> vertex)
            throws IOException, InterruptedException {

        Text vertexID = new Text(String.valueOf(vertex.getVertexId().get()));
        Text edges = new Text();
        String outEdges = new String();

        boolean isFirst = true;
        Iterator<VIntWritable> outEdgesIter =  vertex.iterator();
        while (outEdgesIter.hasNext()) {
            VIntWritable dst = outEdgesIter.next();
            if(isFirst) {
                outEdges += String.valueOf(dst.get());
                isFirst = false;
            } else
                outEdges += ","+String.valueOf(dst.get());
        }

        edges.set(outEdges);

        this.getRecordWriter().write(vertexID, edges);
    }
}
