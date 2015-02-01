package nl.tudelft.graphalytics.neo4j.conn;

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
 * Test case for the connected components implementation on Neo4j.
 *
 * @author Tim Hegeman
 */
public class ConnectedComponentsComputationTest extends AbstractComputationTest {

	@Test
	public void testExample() throws IOException {
		// Load data
		loadGraphFromResource("/test-examples/conn-input");
		// Execute algorithm
		new ConnectedComponentsComputation(graphDatabase).run();
		// Verify output
		Map<Long, Long> expectedOutput = parseOutputResource("/test-examples/conn-output");
		try (Transaction transaction = graphDatabase.beginTx()) {
			for (long vertexId : expectedOutput.keySet()) {
				long component = (long)getNode(vertexId).getProperty(ConnectedComponentsComputation.COMPONENT,
						Long.MAX_VALUE);
				long expectedComponent = expectedOutput.get(vertexId);
				Assert.assertThat("incorrect component computed for id " + vertexId,
						component, is(expectedComponent));
			}
		}
	}

	private static Map<Long, Long> parseOutputResource(String resourceName) throws IOException {
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
				ConnectedComponentsComputationTest.class.getResourceAsStream(resourceName)))) {
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
