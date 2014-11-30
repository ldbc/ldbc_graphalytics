package org.tudelft.graphalytics.giraph.evo;

import java.io.IOException;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.io.formats.TextVertexOutputFormat;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class AdjacencyListWithoutValuesVertexOutputFormat extends TextVertexOutputFormat<LongWritable, Writable, Writable> {

	@Override
	public TextVertexWriter createVertexWriter(TaskAttemptContext context)
			throws IOException, InterruptedException {
		return new AdjacencyListWriter();
	}
	
	private class AdjacencyListWriter extends TextVertexWriterToEachLine {

		@Override
		protected Text convertVertexToLine(Vertex<LongWritable, Writable, Writable> vertex)
				throws IOException {
			StringBuffer sb = new StringBuffer(vertex.getId().toString());
			for (Edge<LongWritable, Writable> edge : vertex.getEdges()) {
				sb.append(' ').append(edge.getTargetVertexId());
			}
			return new Text(sb.toString());
		}
		
	}

}
