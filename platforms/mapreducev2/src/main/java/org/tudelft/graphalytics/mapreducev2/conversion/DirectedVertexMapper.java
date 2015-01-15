package org.tudelft.graphalytics.mapreducev2.conversion;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Maps each directed vertex to a set of edges. Each edge is emitted in both directions, with the direction specified
 * as a flag.
 *
 * @author Tim Hegeman
 */
public class DirectedVertexMapper extends Mapper<LongWritable, Text, LongWritable, EdgeData> {

	/** Pre-compiled regular expression for splitting input lines on whitespace. */
	private static final Pattern whitespacePattern = Pattern.compile("[\t ]");

	@Override
	protected void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		// Parse the line (value) as a list of longs: vertex-id neighbour-1 neighbour-2 ...
		String[] vertices = whitespacePattern.split(value.toString());
		if (vertices.length == 0) {
			context.getCounter(Counters.ParseErrors.INVALID_LINE_FORMAT).increment(1);
			return;
		}
		
		LongWritable sourceId;
		LongWritable destinationId;
		try {
			// Loop through the neighbour IDs and output an edge both ways for each
			sourceId = new LongWritable(Long.parseLong(vertices[0]));
			for (int i = 1; i < vertices.length; i++) {
				destinationId = new LongWritable(Long.parseLong(vertices[1]));
				context.write(sourceId, new EdgeData(destinationId.get(), true));
				context.write(destinationId, new EdgeData(sourceId.get(), false));
			}
		} catch (NumberFormatException ex) {
			context.getCounter(Counters.ParseErrors.NUMBER_FORMAT_EXCEPTION).increment(1);
			return;
		}
	}
	
}
