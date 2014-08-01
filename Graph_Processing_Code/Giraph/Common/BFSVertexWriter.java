package org.test.giraph.utils.writers.generic;

import org.apache.giraph.graph.BasicVertex;
import org.apache.giraph.lib.TextVertexOutputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.mapreduce.RecordWriter;

import java.io.IOException;

/*
 each vertex prints its ID and vertex value which is number of hoops from source vertex
  */
public class BFSVertexWriter extends TextVertexOutputFormat.TextVertexWriter<VIntWritable, Text, VIntWritable> {
    public BFSVertexWriter(
            RecordWriter<Text, Text> lineRecordWriter) {
        super(lineRecordWriter);
    }

    public void writeVertex(BasicVertex<VIntWritable, Text, VIntWritable, ?> vertex)
            throws IOException, InterruptedException {
        Text id = new Text(vertex.getVertexId().toString()+": ");
        Text stepsNr;
        if(vertex.getVertexValue().getLength() > 0)
            stepsNr = new Text(vertex.getVertexValue()+" steps away from source vertex.");
        else
            stepsNr = new Text("UNREACHABLE");
        this.getRecordWriter().write(id, stepsNr);
    }
}
