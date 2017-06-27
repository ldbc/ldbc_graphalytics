/*
 * Copyright 2015 - 2017 Atlarge Research Team,
 * operating at Technische Universiteit Delft
 * and Vrije Universiteit Amsterdam, the Netherlands.
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
package science.atlarge.graphalytics.util.io;

import java.io.IOException;
import java.util.Arrays;

/**
 * Interface for edge list input streams.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
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

		private long sourceId;
		private long destinationId;
		private String[] values;

		public EdgeData() {
			this(0, 0, new String[0]);
		}

		public EdgeData(long sourceId, long destinationId, String[] values) {
			this.sourceId = sourceId;
			this.destinationId = destinationId;
			this.values = values;
		}

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
