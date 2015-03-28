package nl.tudelft.graphalytics.validation.conn;

import nl.tudelft.graphalytics.validation.AbstractValidationTest;
import nl.tudelft.graphalytics.validation.GraphStructure;
import nl.tudelft.graphalytics.validation.GraphValues;
import nl.tudelft.graphalytics.validation.io.LongParser;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Tim Hegeman
 */
public abstract class ConnectedComponentsValidationTest extends AbstractValidationTest {

	public abstract ConnectedComponentsOutput executeDirectedConnectedComponents(
			GraphStructure graph) throws Exception;

	public abstract ConnectedComponentsOutput executeUndirectedConnectedComponents(
			GraphStructure graph) throws Exception;

	@Test
	public final void testDirectedConnectedComponentsOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/conn-dir-input";
		final String outputPath = "/validation-graphs/conn-dir-output";

		GraphStructure inputGraph = parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), true);

		ConnectedComponentsOutput executionResult = executeDirectedConnectedComponents(inputGraph);

		validateConnectedComponents(executionResult, outputPath);
	}

	@Test
	public final void testUndirectedConnectedComponentsOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/conn-undir-input";
		final String outputPath = "/validation-graphs/conn-undir-output";

		GraphStructure inputGraph = parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), false);

		ConnectedComponentsOutput executionResult = executeUndirectedConnectedComponents(inputGraph);

		validateConnectedComponents(executionResult, outputPath);
	}

	private void validateConnectedComponents(ConnectedComponentsOutput executionResult, String outputPath)
			throws Exception {
		GraphValues<Long> outputGraph = parseGraphValuesFromDataset(
				getClass().getResourceAsStream(outputPath), new LongParser());

		assertThat("result graph has the correct number of vertices",
				executionResult.getVertices(), hasSize(outputGraph.getVertices().size()));
		assertThat("result graph has the expected vertex ids",
				executionResult.getVertices(), containsInAnyOrder(outputGraph.getVertices().toArray()));
		for (long vertexId : outputGraph.getVertices()) {
			assertThat("vertex " + vertexId + " has correct value",
					executionResult.getComponentIdForVertex(vertexId),
					is(equalTo(outputGraph.getVertexValue(vertexId))));
		}
	}

}
