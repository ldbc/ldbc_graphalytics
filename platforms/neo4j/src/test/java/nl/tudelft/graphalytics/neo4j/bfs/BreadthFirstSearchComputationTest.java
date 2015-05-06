/**
 * Copyright 2015 Delft University of Technology
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
package nl.tudelft.graphalytics.neo4j.bfs;

import nl.tudelft.graphalytics.domain.Graph;
import nl.tudelft.graphalytics.domain.GraphFormat;
import nl.tudelft.graphalytics.neo4j.AbstractComputationTest;
import org.junit.Assert;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;

/**
 * Test case for the breadth-first search implementation on Neo4j.
 *
 * @author Tim Hegeman
 */
public class BreadthFirstSearchComputationTest extends AbstractComputationTest {

	private static final Long BFS_START_NODE = 1L;

	@Test
	public void testExample() throws IOException {
		// Load data
		loadGraphFromResource("/test-examples/bfs-input");
		String name = "n/a";
		String filePath = "n/a";
		boolean edgeBased = true; // n/a
		long numberOfVertices = -1; // n/a
		long numberOfEdges = -1; // n/a
		Graph directedGraph = new Graph(
				name,
				filePath,
				new GraphFormat(true, edgeBased),
				numberOfVertices,
				numberOfEdges
		);
		Graph undirectedGraph = new Graph(
				name,
				filePath,
				new GraphFormat(false, edgeBased),
				numberOfVertices,
				numberOfEdges
		);

		// Execute algorithm
		runBreadthFirstSearchComputation(new BreadthFirstSearchComputation(graphDatabase, BFS_START_NODE, directedGraph));
		runBreadthFirstSearchComputation(new BreadthFirstSearchComputation(graphDatabase, BFS_START_NODE, undirectedGraph));
	}

	private void runBreadthFirstSearchComputation(BreadthFirstSearchComputation computation) throws IOException {
		// Execute algorithm
		computation.run();
		// Verify output
		Map<Long, Long> expectedOutput = parseOutputResource("/test-examples/bfs-output");
		try (Transaction transaction = graphDatabase.beginTx()) {
			for (long vertexId : expectedOutput.keySet()) {
				long distance = (long) getNode(vertexId).getProperty(BreadthFirstSearchComputation.DISTANCE,
						Long.MAX_VALUE);
				Assert.assertThat("incorrect distance computed for id " + vertexId,
						distance, is(expectedOutput.get(vertexId)));
			}
		}
	}

	private static Map<Long, Long> parseOutputResource(String resourceName) throws IOException {
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
				BreadthFirstSearchComputationTest.class.getResourceAsStream(resourceName)))) {
			Map<Long, Long> expectedOutput = new HashMap<>();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] tokens = line.split(" ");
				expectedOutput.put(Long.parseLong(tokens[0]), Long.parseLong(tokens[1]));
			}
			return expectedOutput;
		}
	}

}
