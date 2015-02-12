package nl.tudelft.graphalytics.neo4j.bfs;

import nl.tudelft.graphalytics.neo4j.AbstractComputationTest;
import org.junit.Assert;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;

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
		// Execute algorithm
		new BreadthFirstSearchComputation(graphDatabase, getNodeId(BFS_START_NODE)).run();
		// Verify output
		Map<Long, Long> expectedOutput = parseOutputResource("/test-examples/bfs-output");
		try (Transaction transaction = graphDatabase.beginTx()) {
			for (long vertexId : expectedOutput.keySet()) {
				long distance = (long)getNode(vertexId).getProperty(BreadthFirstSearchComputation.DISTANCE,
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
