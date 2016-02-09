package nl.tudelft.graphalytics.validation.algorithms.sssp;

import nl.tudelft.graphalytics.domain.algorithms.SingleSourceShortestPathsParameters;
import nl.tudelft.graphalytics.validation.GraphValues;
import nl.tudelft.graphalytics.validation.PropertyGraph;
import nl.tudelft.graphalytics.validation.io.DoubleParser;
import nl.tudelft.graphalytics.validation.io.GraphParser;
import nl.tudelft.graphalytics.validation.io.PropertyGraphParser;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Framework for validating the output of an implementation of the SSSP algorithm. Defines two functions to be
 * implemented to run a platform-specific SSSP implementation on an in-memory graph.
 *
 * @author Tim Hegeman
 */
public abstract class SingleSourceShortestPathsValidationTest {

	/**
	 * Executes the platform-specific implementation of the SSSP algorithm on an in-memory directed graph with given
	 * parameters, and returns the output of the execution.
	 * <p/>
	 * This function is called with sample graphs and the output is compared with known-correct results.
	 *
	 * @param graph      the graph to execute the SSSP algorithm on
	 * @param parameters the values for the algorithm parameters to use
	 * @return the output of the SSSP algorithm
	 * @throws Exception
	 */
	public abstract SingleSourceShortestPathsOutput executeDirectedSingleSourceShortestPaths(
			PropertyGraph<Void, Double> graph, SingleSourceShortestPathsParameters parameters) throws Exception;

	/**
	 * Executes the platform-specific implementation of the SSSP algorithm on an in-memory undirected graph with
	 * given parameters, and returns the output of the execution.
	 * <p/>
	 * This function is called with sample graphs and the output is compared with known-correct results.
	 *
	 * @param graph      the graph to execute the SSSP algorithm on
	 * @param parameters the values for the algorithm parameters to use
	 * @return the output of the SSSP algorithm
	 * @throws Exception
	 */
	public abstract SingleSourceShortestPathsOutput executeUndirectedSingleSourceShortestPaths(
			PropertyGraph<Void, Double> graph, SingleSourceShortestPathsParameters parameters) throws Exception;

	@Test
	public final void testDirectedSingleSourceShortestPathsOnValidationGraph() throws Exception {
		final String vertexInputPath = "/validation-graphs/sssp/dir-input.v";
		final String edgeInputPath = "/validation-graphs/sssp/dir-input.e";
		final String outputPath = "/validation-graphs/sssp/dir-output";
		final long sourceVertex = 1;

		PropertyGraph<Void, Double> inputGraph = loadInput(vertexInputPath, edgeInputPath, true);

		SingleSourceShortestPathsParameters parameters = new SingleSourceShortestPathsParameters("unused", sourceVertex);
		SingleSourceShortestPathsOutput executionResult = executeDirectedSingleSourceShortestPaths(inputGraph, parameters);

		validateSingleSourceShortestPaths(executionResult, outputPath);
	}

	@Test
	public final void testUndirectedSingleSourceShortestPathsOnValidationGraph() throws Exception {
		final String vertexInputPath = "/validation-graphs/sssp/undir-input.v";
		final String edgeInputPath = "/validation-graphs/sssp/undir-input.e";
		final String outputPath = "/validation-graphs/sssp/undir-output";
		final long sourceVertex = 1;

		PropertyGraph<Void, Double> inputGraph = loadInput(vertexInputPath, edgeInputPath, false);

		SingleSourceShortestPathsParameters parameters = new SingleSourceShortestPathsParameters("unused", sourceVertex);
		SingleSourceShortestPathsOutput executionResult = executeUndirectedSingleSourceShortestPaths(inputGraph, parameters);

		validateSingleSourceShortestPaths(executionResult, outputPath);
	}

	private void validateSingleSourceShortestPaths(SingleSourceShortestPathsOutput executionResult, String outputPath) throws IOException {
		final double EPSILON = 1e-4;

		GraphValues<Double> outputGraph = GraphParser.parseGraphValuesFromDataset(
				getClass().getResourceAsStream(outputPath), new DoubleParser());

		assertThat("result graph has the correct number of vertices",
				executionResult.getVertices(), hasSize(outputGraph.getVertices().size()));
		assertThat("result graph has the expected vertex ids",
				executionResult.getVertices(), containsInAnyOrder(outputGraph.getVertices().toArray()));
		for (long vertexId : outputGraph.getVertices()) {
			double expectedValue = outputGraph.getVertexValue(vertexId);
			double actualValue = executionResult.getDistanceForVertex(vertexId);
			assertThat("vertex " + vertexId + " has correct value",
					actualValue, is(closeTo(expectedValue, expectedValue * EPSILON)));
		}
	}

	private PropertyGraph<Void, Double> loadInput(String vertexFile, String edgeFile, boolean isDirected) throws IOException {
		return PropertyGraphParser.parserPropertyGraph(
				getClass().getResourceAsStream(vertexFile),
				getClass().getResourceAsStream(edgeFile),
				isDirected,
				new PropertyGraphParser.ValueParser<Void>() {
					@Override
					public Void parse(String[] valueTokens) throws IOException {
						return null;
					}
				},
				new PropertyGraphParser.ValueParser<Double>() {
					@Override
					public Double parse(String[] valueTokens) throws IOException {
						return Double.parseDouble(valueTokens[0]);
					}
				}
		);
	}

}
