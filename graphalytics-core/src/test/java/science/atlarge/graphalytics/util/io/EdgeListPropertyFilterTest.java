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
 * Test cases for EdgeListPropertyFilter.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class EdgeListPropertyFilterTest {

	private static final EdgeListStream.EdgeData[] input = new EdgeListStream.EdgeData[]{
			new EdgeListStream.EdgeData(0, 1, new String[]{"p0.1", "p0.2", "p0.3", "p0.4"}),
			new EdgeListStream.EdgeData(1, 2, new String[]{"p1.1", "p1.2", "p1.3"}),
			new EdgeListStream.EdgeData(10, 1, new String[]{"p10.1", "p10.2", "p10.3", "p10.4", "p10.5"})
	};

	@Test
	public void testEmptyFilterRemovesAllProperties() throws IOException {
		EdgeListStream.EdgeData[] expectedOutput = new EdgeListStream.EdgeData[]{
				new EdgeListStream.EdgeData(0, 1, new String[0]),
				new EdgeListStream.EdgeData(1, 2, new String[0]),
				new EdgeListStream.EdgeData(10, 1, new String[0])
		};

		EdgeListStream edgeListStream = new MockEdgeListStream(input);

		try (EdgeListPropertyFilter filter = new EdgeListPropertyFilter(edgeListStream, new int[0])) {
			EdgeListStreamUtility.validateEdgeListStreamOutput(filter, expectedOutput);
		}
	}

	@Test
	public void testFilterCanReorderProperties() throws IOException {
		EdgeListStream.EdgeData[] expectedOutput = new EdgeListStream.EdgeData[]{
				new EdgeListStream.EdgeData(0, 1, new String[]{"p0.3", "p0.1"}),
				new EdgeListStream.EdgeData(1, 2, new String[]{"p1.3", "p1.1"}),
				new EdgeListStream.EdgeData(10, 1, new String[]{"p10.3", "p10.1"})
		};

		EdgeListStream edgeListStream = new MockEdgeListStream(input);

		try (EdgeListPropertyFilter filter = new EdgeListPropertyFilter(edgeListStream, new int[]{2, 0})) {
			EdgeListStreamUtility.validateEdgeListStreamOutput(filter, expectedOutput);
		}
	}

	@Test
	public void testFilterCanDuplicateProperties() throws IOException {
		EdgeListStream.EdgeData[] expectedOutput = new EdgeListStream.EdgeData[]{
				new EdgeListStream.EdgeData(0, 1, new String[]{"p0.2", "p0.2"}),
				new EdgeListStream.EdgeData(1, 2, new String[]{"p1.2", "p1.2"}),
				new EdgeListStream.EdgeData(10, 1, new String[]{"p10.2", "p10.2"})
		};

		EdgeListStream edgeListStream = new MockEdgeListStream(input);

		try (EdgeListPropertyFilter filter = new EdgeListPropertyFilter(edgeListStream, new int[]{1, 1})) {
			EdgeListStreamUtility.validateEdgeListStreamOutput(filter, expectedOutput);
		}
	}

}
