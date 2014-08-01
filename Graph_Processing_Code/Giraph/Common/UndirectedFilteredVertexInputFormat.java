package org.test.giraph.utils.readers.undirected;

import org.apache.giraph.graph.VertexReader;
import org.apache.giraph.lib.TextVertexInputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;

public class UndirectedFilteredVertexInputFormat extends TextVertexInputFormat<VIntWritable, Text, VIntWritable, Text> {
    @Override
    public VertexReader<VIntWritable, Text, VIntWritable, Text> createVertexReader(InputSplit split,TaskAttemptContext context)
            throws IOException {
        return new UndirectedFilteredVertexReader(textInputFormat.createRecordReader(split, context));
    }
}

