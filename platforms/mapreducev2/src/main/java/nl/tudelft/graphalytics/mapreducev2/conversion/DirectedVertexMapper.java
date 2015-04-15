/**
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics.mapreducev2.conversion;

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
		} else if (vertices.length == 1) {
			long vertexId = Long.parseLong(vertices[0]);
			context.write(new LongWritable(vertexId), new EdgeData(vertexId, true));
			return;
		}

		LongWritable sourceId;
		LongWritable destinationId;
		try {
			// Loop through the neighbour IDs and output an edge both ways for each
			sourceId = new LongWritable(Long.parseLong(vertices[0]));
			for (int i = 1; i < vertices.length; i++) {
				destinationId = new LongWritable(Long.parseLong(vertices[i]));
				context.write(sourceId, new EdgeData(destinationId.get(), true));
				context.write(destinationId, new EdgeData(sourceId.get(), false));
			}
		} catch (NumberFormatException ex) {
			context.getCounter(Counters.ParseErrors.NUMBER_FORMAT_EXCEPTION).increment(1);
			return;
		}
	}
	
}
