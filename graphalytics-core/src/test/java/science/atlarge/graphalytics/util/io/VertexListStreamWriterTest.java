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

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for VertexListStreamWriter.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class VertexListStreamWriterTest {

	private static final VertexListStream.VertexData[] vertices = new VertexListStream.VertexData[]{
			new VertexListStream.VertexData(0, new String[]{"testing"}),
			new VertexListStream.VertexData(1, new String[]{"multiple", "properties"}),
			new VertexListStream.VertexData(10, new String[]{"123"})
	};

	private static final String expectedOutput = "0 testing\n" +
			"1 multiple properties\n" +
			"10 123\n";

	@Test
	public void testWriteAllOnMockVertexListStream() throws IOException {
		VertexListStream vertexListStream = new MockVertexListStream(vertices);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try (VertexListStreamWriter writer = new VertexListStreamWriter(vertexListStream, outputStream)) {
			writer.writeAll();
			assertEquals("Output of VertexListStreamWriter is correct", expectedOutput, outputStream.toString());
		}
	}

}
