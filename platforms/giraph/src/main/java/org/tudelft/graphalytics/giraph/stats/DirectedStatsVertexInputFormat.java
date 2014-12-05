package org.tudelft.graphalytics.giraph.stats;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.edge.EdgeFactory;
import org.apache.giraph.io.formats.TextVertexInputFormat;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.google.common.collect.Lists;

/**
 * Based on {@link org.apache.giraph.io.formats.LongLongNullTextInputFormat}.
 */
public class DirectedStatsVertexInputFormat extends
		TextVertexInputFormat<LongWritable, IntWritable, NullWritable> {

	private static final Pattern SEPARATOR = Pattern.compile("[\t ]");

	@Override
	public TextVertexReader createVertexReader(InputSplit split, TaskAttemptContext context)
			throws IOException {
		return new StatsVertexReader();
	}

	public class StatsVertexReader extends
			TextVertexReaderFromEachLineProcessed<String[]> {
		/** Cached vertex id for the current line */
		private LongWritable id;
		private IntWritable value = new IntWritable();

		@Override
		protected String[] preprocessLine(Text line) throws IOException {
			String[] tokens = SEPARATOR.split(line.toString());
			id = new LongWritable(Long.parseLong(tokens[0]));
			return tokens;
		}

		@Override
		protected LongWritable getId(String[] tokens) throws IOException {
			return id;
		}

		@Override
		protected IntWritable getValue(String[] tokens) throws IOException {
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
