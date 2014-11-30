package org.tudelft.graphalytics.giraph.conversion;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class DirectedEdgeMapper extends Mapper<LongWritable, Text, LongWritable, LongWritable> {

	/** Pre-compiled regular expression for splitting input lines on whitespace. */
	private static final Pattern whitespacePattern = Pattern.compile("[\t ]");
	
	private LongWritable sourceId = new LongWritable();
	private LongWritable destinationId = new LongWritable();
	
	@Override
	protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		// Parse the line (value) as a pair of longs: source-vertex destination-vertex
		String[] vertices = whitespacePattern.split(value.toString());
		if (vertices.length != 2) {
			context.getCounter(Counters.ParseErrors.INVALID_LINE_FORMAT).increment(1);
			return;
		}
		
		// Parse both longs
		try {
			sourceId.set(Long.parseLong(vertices[0]));
			destinationId.set(Long.parseLong(vertices[1]));
		} catch (NumberFormatException ex) {
			context.getCounter(Counters.ParseErrors.NUMBER_FORMAT_EXCEPTION).increment(1);
			return;
		}
		
		// Output the edge
		context.write(sourceId, destinationId);
	}
	
}
