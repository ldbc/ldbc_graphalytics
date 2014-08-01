package org.test.giraph.utils.writers.undirected;

import org.apache.giraph.graph.BasicVertex;
import org.apache.giraph.lib.TextVertexOutputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.mapreduce.RecordWriter;

import java.io.IOException;

/*
    Outputs Vertex-filtered format with component label attached at the end
 */
public class UndirectedFilteredLabeledVertexWriter extends TextVertexOutputFormat.TextVertexWriter<VIntWritable, Text, VIntWritable> {
    /**
     * Vertex writer with the internal line writer.
     *
     * @param lineRecordWriter Wil actually be written to.
     */
    public UndirectedFilteredLabeledVertexWriter(
            RecordWriter<Text, Text> lineRecordWriter) {
        super(lineRecordWriter);
    }

    public void writeVertex(BasicVertex<VIntWritable, Text, VIntWritable, ?> vertex)
            throws IOException, InterruptedException {
        getRecordWriter().write(vertex.getVertexValue(), null);
    }
}
