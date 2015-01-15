package nl.tudelft.graphalytics.mapreducev2.conversion;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Reduces a set of incoming edges for a vertex to a single line of ourput.
 *
 * @author Tim Hegeman
 */
public class UndirectedVertexReducer extends Reducer<LongWritable, LongWritable, NullWritable, Text> {

	private Text outValue = new Text();
	
	@Override
	protected void reduce(LongWritable key, Iterable<LongWritable> values, Context context)
			throws IOException, InterruptedException {
		// Combine the vertex ID and neighbour IDs using a StringBuilder
		StringBuilder sb = new StringBuilder();
		sb.append(key.get());
		for (LongWritable neighbour : values) {
			sb.append(' ').append(neighbour.get());
		}
		
		// Output the constructed line
		outValue.set(sb.toString());
		context.write(NullWritable.get(), outValue);
	}
	
}
