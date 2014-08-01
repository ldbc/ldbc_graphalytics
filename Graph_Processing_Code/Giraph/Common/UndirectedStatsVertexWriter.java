package org.test.giraph.utils.writers.undirected;

import org.apache.giraph.graph.BasicVertex;
import org.apache.giraph.lib.TextVertexOutputFormat;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.test.giraph.jobs.undirected.UndirectedStatsJob;

import java.io.IOException;

public class UndirectedStatsVertexWriter extends TextVertexOutputFormat.TextVertexWriter<VIntWritable, Text, VIntWritable> {
    /**
     * Vertex writer with the internal line writer.
     *
     * @param lineRecordWriter Wil actually be written to.
     */
    public UndirectedStatsVertexWriter(
            RecordWriter<Text, Text> lineRecordWriter) {
        super(lineRecordWriter);
    }

    public void writeVertex(BasicVertex<VIntWritable, Text, VIntWritable, ?> vertex)
            throws IOException, InterruptedException {

        if(vertex.getVertexId().equals(new VIntWritable(getContext().getConfiguration().getInt(UndirectedStatsJob.MAIN_ID, -1))))
            getRecordWriter().write(vertex.getVertexValue(), null);
        else
            getRecordWriter().write(null, null);
    }
}