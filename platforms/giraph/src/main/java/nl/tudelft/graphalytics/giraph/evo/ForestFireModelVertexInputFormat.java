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
package nl.tudelft.graphalytics.giraph.evo;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.edge.EdgeFactory;
import org.apache.giraph.io.formats.TextVertexInputFormat;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.google.common.collect.Lists;

/**
 * Based on {@link org.apache.giraph.io.formats.LongLongNullTextInputFormat}.
 *
 * @author Tim Hegeman
 */
public class ForestFireModelVertexInputFormat extends
		TextVertexInputFormat<LongWritable, ForestFireModelData, NullWritable> {

	private static final Pattern SEPARATOR = Pattern.compile("[\t ]");

	@Override
	public TextVertexReader createVertexReader(InputSplit split, TaskAttemptContext context)
			throws IOException {
		return new ForestFireModelVertexReader();
	}

	public class ForestFireModelVertexReader extends
			TextVertexReaderFromEachLineProcessed<String[]> {
		/** Cached vertex id for the current line */
		private LongWritable id;
		private ForestFireModelData value;

		@Override
		protected String[] preprocessLine(Text line) throws IOException {
			String[] tokens = SEPARATOR.split(line.toString());
			id = new LongWritable(Long.parseLong(tokens[0]));
			value = new ForestFireModelData();
			return tokens;
		}

		@Override
		protected LongWritable getId(String[] tokens) throws IOException {
			return id;
		}

		@Override
		protected ForestFireModelData getValue(String[] tokens) throws IOException {
			return value;
		}

		@Override
		protected Iterable<Edge<LongWritable, NullWritable>> getEdges(
				String[] tokens) throws IOException {
			List<Edge<LongWritable, NullWritable>> edges = Lists
					.newArrayListWithCapacity(tokens.length - 1);
			for (int n = 1; n < tokens.length; n++) {
				edges.add(EdgeFactory.create(new LongWritable(Long
						.parseLong(tokens[n]))));
			}
			return edges;
		}
	}

}
