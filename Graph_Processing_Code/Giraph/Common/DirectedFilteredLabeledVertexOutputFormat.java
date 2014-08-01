package org.test.giraph.utils.writers.directed;

import org.apache.giraph.graph.VertexWriter;
import org.apache.giraph.lib.TextVertexOutputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;

/**
 */
public class DirectedFilteredLabeledVertexOutputFormat extends TextVertexOutputFormat<VIntWritable, Text, VIntWritable> {
    @Override
    public VertexWriter<VIntWritable, Text, VIntWritable>
    createVertexWriter(TaskAttemptContext context)
            throws IOException, InterruptedException {
        RecordWriter<Text, Text> recordWriter =
                textOutputFormat.getRecordWriter(context);
        return new DirectedFilteredLabeledVertexWriter(recordWriter);
    }
}
