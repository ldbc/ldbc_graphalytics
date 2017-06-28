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
 * Test cases for EdgeListInputStreamReader.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class EdgeListInputStreamReaderTest {

	@Test
	public void testHasNextEdgeOnEmptyLines() throws IOException {
		String input = "\n  \n	\n";
		InputStream inputStream = new ByteArrayInputStream(input.getBytes());

		try (EdgeListInputStreamReader reader = new EdgeListInputStreamReader(inputStream)) {
			assertFalse(reader.hasNextEdge());
		}
	}

	@Test
	public void testGetNextEdgeOnSampleFile() throws IOException {
		String input = "0 1 1.23 property\n1 2 variable number of properties\n10 1\n";
		InputStream inputStream = new ByteArrayInputStream(input.getBytes());

		EdgeListStream.EdgeData[] expectedOutput = new EdgeListStream.EdgeData[]{
				new EdgeListStream.EdgeData(0, 1, new String[]{"1.23", "property"}),
				new EdgeListStream.EdgeData(1, 2, new String[]{"variable", "number", "of", "properties"}),
				new EdgeListStream.EdgeData(10, 1, new String[0])
		};

		try (EdgeListInputStreamReader reader = new EdgeListInputStreamReader(inputStream)) {
			EdgeListStreamUtility.validateEdgeListStreamOutput(reader, expectedOutput);
		}
	}

}
