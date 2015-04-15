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
package nl.tudelft.graphalytics.giraph.io;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.giraph.io.EdgeReader;
import org.apache.giraph.io.formats.TextEdgeInputFormat;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * Input format for edge-based directed graphs. Inspired by IntNullTextEdgeInputFormat
 * provided by Giraph.
 *
 * @author Tim Hegeman
 */
public class DirectedLongNullTextEdgeInputFormat extends TextEdgeInputFormat<LongWritable, NullWritable> {

	private static final Pattern SEPARATOR = Pattern.compile("[\t ]");
	
	@Override
	public EdgeReader<LongWritable, NullWritable> createEdgeReader(
			InputSplit split, TaskAttemptContext context) throws IOException {
		return new LongNullEdgeReader();
	}
	
	private class LongNullEdgeReader extends TextEdgeReaderFromEachLineProcessed<LongPair> {

		@Override
		protected LongPair preprocessLine(Text line) throws IOException {
			String[] tokens = SEPARATOR.split(line.toString());
			long source = Long.parseLong(tokens[0]);
			long destination = Long.parseLong(tokens[1]);
			return new LongPair(source, destination);
		}

		@Override
		protected LongWritable getTargetVertexId(LongPair line)
				throws IOException {
			return new LongWritable(line.getSecond());
		}

		@Override
		protected LongWritable getSourceVertexId(LongPair line)
				throws IOException {
			return new LongWritable(line.getFirst());
		}

		@Override
		protected NullWritable getValue(LongPair line) throws IOException {
			return NullWritable.get();
		}

	}

}
