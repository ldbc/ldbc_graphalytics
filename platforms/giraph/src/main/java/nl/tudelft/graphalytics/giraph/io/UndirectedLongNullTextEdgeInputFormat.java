package nl.tudelft.graphalytics.giraph.io;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.edge.EdgeFactory;
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
public class UndirectedLongNullTextEdgeInputFormat extends TextEdgeInputFormat<LongWritable, NullWritable> {

	private static final Pattern SEPARATOR = Pattern.compile("[\t ]");
	
	@Override
	public EdgeReader<LongWritable, NullWritable> createEdgeReader(
			InputSplit split, TaskAttemptContext context) throws IOException {
		return new LongNullEdgeReader();
	}
	
	private class LongNullEdgeReader extends TextEdgeReader {

		private boolean outputBackwards = true;
		private long first;
		private long second;		

		@Override
		public boolean nextEdge() throws IOException, InterruptedException {
			if (!outputBackwards) {
				outputBackwards = true;
				return true;
			}

			if (!getRecordReader().nextKeyValue())
				return false;

			String[] tokens = SEPARATOR.split(getRecordReader().getCurrentValue().toString());
			first = Long.parseLong(tokens[0]);
			second = Long.parseLong(tokens[1]);
			outputBackwards = false;
			return true;
		}

		@Override
		public LongWritable getCurrentSourceId() throws IOException, InterruptedException {
			return new LongWritable(outputBackwards ? second : first);
		}

		@Override
		public Edge<LongWritable, NullWritable> getCurrentEdge() throws IOException, InterruptedException {
			return EdgeFactory.create(new LongWritable(outputBackwards ? first : second));
		}

	}

}
