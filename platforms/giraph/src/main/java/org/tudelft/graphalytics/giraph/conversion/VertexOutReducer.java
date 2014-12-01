package org.tudelft.graphalytics.giraph.conversion;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class VertexOutReducer extends Reducer<LongWritable, LongWritable, NullWritable, Text> {

	private Text outValue = new Text();
	
	@Override
	protected void reduce(LongWritable key, Iterable<LongWritable> values, Context context)
			throws IOException, InterruptedException {
		// Combine the vertex ID and neighbour IDs using a StringBuilder
		StringBuilder sb = new StringBuilder();
		sb.append(key.get());
		for (LongWritable neighbour : values) {
			// Add any edge that points to a different vertex
			if (neighbour.get() != key.get())
				sb.append(' ').append(neighbour.get());
		}
		
		// Output the constructed line
		outValue.set(sb.toString());
		context.write(NullWritable.get(), outValue);
	}
	
}
