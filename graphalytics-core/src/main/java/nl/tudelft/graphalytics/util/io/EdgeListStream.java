package nl.tudelft.graphalytics.util.io;

import java.io.IOException;
import java.util.Arrays;

/**
 * Interface for edge list input streams.
 *
 * @author Tim Hegeman
 */
public interface EdgeListStream extends AutoCloseable {

	/**
	 * @return true iff there is another edge available in the stream
	 * @throws IOException if an error occurred while reading from or parsing the input stream
	 */
	boolean hasNextEdge() throws IOException;

	/**
	 * Reads the next edge from the stream. Warning: the returned EdgeData object may be reused for the next call
	 * to getNextEdge to significantly decrease garbage collection overhead. Throws a NoSuchElementException if no
	 * next element exists.
	 *
	 * @return the next edge
	 * @throws IOException if an error occurred while reading from or parsing the input stream
	 */
	EdgeData getNextEdge() throws IOException;

	@Override
	void close() throws IOException;

	class EdgeData {

		private long sourceId = 0;
		private long destinationId = 0;
		private String[] values = new String[0];

		public long getSourceId() {
			return sourceId;
		}

		protected void setSourceId(long sourceId) {
			this.sourceId = sourceId;
		}

		public long getDestinationId() {
			return destinationId;
		}

		protected void setDestinationId(long destinationId) {
			this.destinationId = destinationId;
		}

		public String[] getValues() {
			return values;
		}

		protected void setValues(String[] values) {
			this.values = values;
		}

		@Override
		public String toString() {
			return "EdgeData{" +
					"sourceId=" + sourceId +
					", destinationId=" + destinationId +
					", values=" + Arrays.toString(values) +
					'}';
		}

	}

}
