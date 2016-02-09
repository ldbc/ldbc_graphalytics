package nl.tudelft.graphalytics.util.io;

import java.io.IOException;
import java.util.Arrays;

/**
 * Interface for vertex list input streams.
 *
 * @author Tim Hegeman
 */
public interface VertexListStream extends AutoCloseable {

	/**
	 * @return true iff there is another vertex available in the stream
	 * @throws IOException if an error occurred while reading from or parsing the input stream
	 */
	boolean hasNextVertex() throws IOException;

	/**
	 * Reads the next vertex from the stream. Warning: the returned VertexData object may be reused for the next call
	 * to getNextVertex to significantly decrease garbage collection overhead. Throws a NoSuchElementException if no
	 * next element exists.
	 *
	 * @return the next vertex
	 * @throws IOException if an error occurred while reading from or parsing the input stream
	 */
	VertexData getNextVertex() throws IOException;

	@Override
	void close() throws IOException;

	class VertexData {

		private long id = 0;
		private String[] values = new String[0];

		public long getId() {
			return id;
		}

		protected void setId(long id) {
			this.id = id;
		}

		public String[] getValues() {
			return values;
		}

		protected void setValues(String[] values) {
			this.values = values;
		}

		@Override
		public String toString() {
			return "VertexData{" +
					"id=" + id +
					", values=" + Arrays.toString(values) +
					'}';
		}

	}

}
