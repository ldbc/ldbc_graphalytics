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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertFalse;

/**
 * Test cases for VertexListInputStreamReader.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class VertexListInputStreamReaderTest {

	@Test
	public void testHasNextVertexOnEmptyLines() throws IOException {
		String input = "\n  \n	\n";
		InputStream inputStream = new ByteArrayInputStream(input.getBytes());

		try (VertexListInputStreamReader reader = new VertexListInputStreamReader(inputStream)) {
			assertFalse(reader.hasNextVertex());
		}
	}

	@Test
	public void testGetNextVertexOnSampleFile() throws IOException {
		String input = "0 1.23 property\n1 variable number of properties\n10\n";
		InputStream inputStream = new ByteArrayInputStream(input.getBytes());

		VertexListStream.VertexData[] expectedOutput = new VertexListStream.VertexData[]{
				new VertexListStream.VertexData(0, new String[]{"1.23", "property"}),
				new VertexListStream.VertexData(1, new String[]{"variable", "number", "of", "properties"}),
				new VertexListStream.VertexData(10, new String[0])
		};

		try (VertexListInputStreamReader reader = new VertexListInputStreamReader(inputStream)) {
			VertexListStreamUtility.validateVertexListStreamOutput(reader, expectedOutput);
		}
	}

}
