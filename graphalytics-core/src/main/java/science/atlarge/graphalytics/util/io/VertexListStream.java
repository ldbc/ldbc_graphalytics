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
 * Interface for vertex list input streams.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
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

		private long id;
		private String[] values;

		public VertexData() {
			this(0, new String[0]);
		}

		public VertexData(long id, String[] values) {
			this.id = id;
			this.values = values;
		}

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
