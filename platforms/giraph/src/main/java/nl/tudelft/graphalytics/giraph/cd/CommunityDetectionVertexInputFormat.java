package nl.tudelft.graphalytics.giraph.cd;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.edge.EdgeFactory;
import org.apache.giraph.io.formats.TextVertexInputFormat;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.google.common.collect.Lists;

/**
 * Based on Giraph's
 * {@link org.apache.giraph.io.formats.LongLongNullTextInputFormat LongLongNullTextInputFormat}.
 *
 * @author Tim Hegeman
 */
public abstract class CommunityDetectionVertexInputFormat<E extends Writable> extends
		TextVertexInputFormat<LongWritable, CDLabel, E> {

	private static final Pattern SEPARATOR = Pattern.compile("[\t ]");

	@Override
	public TextVertexReader createVertexReader(InputSplit split, TaskAttemptContext context)
			throws IOException {
		return new CommunityDetectionVertexReader();
	}

	protected abstract E defaultValue();

	public class CommunityDetectionVertexReader extends
			TextVertexReaderFromEachLineProcessed<String[]> {
		/** Cached vertex id for the current line */
		private LongWritable id;
		private CDLabel value;

		@Override
		protected String[] preprocessLine(Text line) throws IOException {
			String[] tokens = SEPARATOR.split(line.toString());
			id = new LongWritable(Long.parseLong(tokens[0]));
			value = new CDLabel();
			return tokens;
		}

		@Override
		protected LongWritable getId(String[] tokens) throws IOException {
			return id;
		}

		@Override
		protected CDLabel getValue(String[] tokens) throws IOException {
			return value;
		}

		@Override
		protected Iterable<Edge<LongWritable, E>> getEdges(
				String[] tokens) throws IOException {
			List<Edge<LongWritable, E>> edges = Lists
					.newArrayListWithCapacity(tokens.length - 1);
			for (int n = 1; n < tokens.length; n++) {
				edges.add(EdgeFactory.create(new LongWritable(Long
						.parseLong(tokens[n])), defaultValue()));
			}
			return edges;
		}
	}

	public static class Undirected extends CommunityDetectionVertexInputFormat<NullWritable> {

		@Override
		protected NullWritable defaultValue() {
			return NullWritable.get();
		}

	}

	public static class Directed extends CommunityDetectionVertexInputFormat<BooleanWritable> {

		@Override
		protected BooleanWritable defaultValue() {
			return new BooleanWritable(false);
		}

	}

}
