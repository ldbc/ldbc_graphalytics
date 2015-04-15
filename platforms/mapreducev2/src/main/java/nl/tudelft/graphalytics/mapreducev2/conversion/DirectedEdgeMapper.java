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
 * Maps each directed edge in the input to an edge in both directions. The direction and target make up the value class.
 *
 * @author Tim Hegeman
 */
public class DirectedEdgeMapper extends Mapper<LongWritable, Text, LongWritable, EdgeData> {

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
		
		// Output the edge both ways
		context.write(sourceId, new EdgeData(destinationId.get(), true));
		context.write(destinationId, new EdgeData(sourceId.get(), false));
	}
	
}
