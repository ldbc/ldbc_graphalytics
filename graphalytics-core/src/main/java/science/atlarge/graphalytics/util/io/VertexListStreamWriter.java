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

import java.io.*;

/**
 * Writes a VertexListStream to an OutputStream in the EVLP vertex-list format.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class VertexListStreamWriter implements AutoCloseable {

	private final VertexListStream inputStream;
	private final Writer outputWriter;

	public VertexListStreamWriter(VertexListStream inputStream, OutputStream outputStream) {
		this.inputStream = inputStream;
		this.outputWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
	}

	public void writeAll() throws IOException {
		while (inputStream.hasNextVertex()) {
			writeNextVertex();
		}
		outputWriter.flush();
	}

	private void writeNextVertex() throws IOException {
		VertexListStream.VertexData vertexData = inputStream.getNextVertex();
		outputWriter.write(String.valueOf(vertexData.getId()));
		for (String value : vertexData.getValues()) {
			outputWriter.append(' ');
			outputWriter.write(value);
		}
		outputWriter.append('\n');
	}

	@Override
	public void close() throws IOException {
		outputWriter.close();
	}

}
