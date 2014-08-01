package org.test.giraph.utils.writers.generic;

import org.apache.giraph.graph.VertexWriter;
import org.apache.giraph.lib.TextVertexOutputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.test.giraph.utils.writers.undirected.UndirectedStatsVertexWriter;

import java.io.IOException;

/**
 Dumps vertex graph data. ONLY ID and OutEdges
 */
public class OutEdgesVertexOutputFormat extends TextVertexOutputFormat<VIntWritable, Text, VIntWritable> {
    @Override
    public VertexWriter<VIntWritable, Text, VIntWritable>
    createVertexWriter(TaskAttemptContext context)
            throws IOException, InterruptedException {
        RecordWriter<Text, Text> recordWriter =
                textOutputFormat.getRecordWriter(context);
        return new OutEdgesVertexWriter(recordWriter);
    }
}