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
package nl.tudelft.graphalytics.giraph.cd;

import nl.tudelft.graphalytics.giraph.io.LongPair;
import org.apache.giraph.io.EdgeReader;
import org.apache.giraph.io.formats.TextEdgeInputFormat;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Similar to {@link nl.tudelft.graphalytics.giraph.io.DirectedLongNullTextEdgeInputFormat
 * DirectedLongNullTextEdgeInputFormat}, except that edges have {@link BooleanWritable} values.
 *
 * @author Tim Hegeman
 */
public class DirectedCommunityDetectionEdgeInputFormat extends TextEdgeInputFormat<LongWritable, BooleanWritable> {

	private static final Pattern SEPARATOR = Pattern.compile("[\t ]");

	@Override
	public EdgeReader<LongWritable, BooleanWritable> createEdgeReader(
			InputSplit split, TaskAttemptContext context) throws IOException {
		return new LongBooleanEdgeReader();
	}

	private class LongBooleanEdgeReader extends TextEdgeReaderFromEachLineProcessed<LongPair> {

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
		protected BooleanWritable getValue(LongPair line) throws IOException {
			return new BooleanWritable(false);
		}

	}

}
