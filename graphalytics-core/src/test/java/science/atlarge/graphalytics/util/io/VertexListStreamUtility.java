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

import static org.junit.Assert.*;

/**
 * Utility class for validating a VertexListStream implementation
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class VertexListStreamUtility {

	public static void validateVertexListStreamOutput(VertexListStream stream, VertexListStream.VertexData[] expectedOutput) throws IOException {
		for (VertexListStream.VertexData expectedVertex : expectedOutput) {
			assertTrue(stream.hasNextVertex());
			VertexListStream.VertexData vertex = stream.getNextVertex();

			assertEquals(expectedVertex.getId(), vertex.getId());
			assertArrayEquals(expectedVertex.getValues(), vertex.getValues());
		}
		assertFalse(stream.hasNextVertex());
	}

}
