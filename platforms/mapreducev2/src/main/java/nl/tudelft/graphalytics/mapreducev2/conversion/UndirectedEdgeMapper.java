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
 * Maps each undirected edge in the undirected input to a directed edge for either direction.
 *
 * @author Tim Hegeman
 */
public class UndirectedEdgeMapper extends Mapper<LongWritable, Text, LongWritable, LongWritable> {

	/** Pre-compiled regular expression for splitting input lines on whitespace. */
	private static final Pattern whitespacePattern = Pattern.compile("[\t ]");
	
	private LongWritable leftId = new LongWritable();
	private LongWritable rightId = new LongWritable();
	
	@Override
	protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		// Parse the line (value) as a pair of longs: vertex-A vertex-B
		String[] vertices = whitespacePattern.split(value.toString());
		if (vertices.length != 2) {
			context.getCounter(Counters.ParseErrors.INVALID_LINE_FORMAT).increment(1);
			return;
		}
		
		// Parse both longs
		try {
			leftId.set(Long.parseLong(vertices[0]));
			rightId.set(Long.parseLong(vertices[1]));
		} catch (NumberFormatException ex) {
			context.getCounter(Counters.ParseErrors.NUMBER_FORMAT_EXCEPTION).increment(1);
			return;
		}
		
		// Output the edge both ways
		context.write(leftId, rightId);
		context.write(rightId, leftId);
	}
	
}
