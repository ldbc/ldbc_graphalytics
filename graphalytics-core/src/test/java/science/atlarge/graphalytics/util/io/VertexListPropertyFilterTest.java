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

import java.io.IOException;

/**
 * Test cases for VertexListPropertyFilter.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class VertexListPropertyFilterTest {

	private static final VertexListStream.VertexData[] input = new VertexListStream.VertexData[]{
			new VertexListStream.VertexData(0, new String[]{"p0.1", "p0.2", "p0.3", "p0.4"}),
			new VertexListStream.VertexData(1, new String[]{"p1.1", "p1.2", "p1.3"}),
			new VertexListStream.VertexData(10, new String[]{"p10.1", "p10.2", "p10.3", "p10.4", "p10.5"})
	};

	@Test
	public void testEmptyFilterRemovesAllProperties() throws IOException {
		VertexListStream.VertexData[] expectedOutput = new VertexListStream.VertexData[]{
				new VertexListStream.VertexData(0, new String[0]),
				new VertexListStream.VertexData(1, new String[0]),
				new VertexListStream.VertexData(10, new String[0])
		};

		VertexListStream vertexListStream = new MockVertexListStream(input);

		try (VertexListPropertyFilter filter = new VertexListPropertyFilter(vertexListStream, new int[0])) {
			VertexListStreamUtility.validateVertexListStreamOutput(filter, expectedOutput);
		}
	}

	@Test
	public void testFilterCanReorderProperties() throws IOException {
		VertexListStream.VertexData[] expectedOutput = new VertexListStream.VertexData[]{
				new VertexListStream.VertexData(0, new String[]{"p0.3", "p0.1"}),
				new VertexListStream.VertexData(1, new String[]{"p1.3", "p1.1"}),
				new VertexListStream.VertexData(10, new String[]{"p10.3", "p10.1"})
		};

		VertexListStream vertexListStream = new MockVertexListStream(input);

		try (VertexListPropertyFilter filter = new VertexListPropertyFilter(vertexListStream, new int[]{2, 0})) {
			VertexListStreamUtility.validateVertexListStreamOutput(filter, expectedOutput);
		}
	}

	@Test
	public void testFilterCanDuplicateProperties() throws IOException {
		VertexListStream.VertexData[] expectedOutput = new VertexListStream.VertexData[]{
				new VertexListStream.VertexData(0, new String[]{"p0.2", "p0.2"}),
				new VertexListStream.VertexData(1, new String[]{"p1.2", "p1.2"}),
				new VertexListStream.VertexData(10, new String[]{"p10.2", "p10.2"})
		};

		VertexListStream vertexListStream = new MockVertexListStream(input);

		try (VertexListPropertyFilter filter = new VertexListPropertyFilter(vertexListStream, new int[]{1, 1})) {
			VertexListStreamUtility.validateVertexListStreamOutput(filter, expectedOutput);
		}
	}

}
