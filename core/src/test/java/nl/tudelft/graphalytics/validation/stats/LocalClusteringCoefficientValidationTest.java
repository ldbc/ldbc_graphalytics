package nl.tudelft.graphalytics.validation.stats;

import nl.tudelft.graphalytics.validation.GraphStructure;
import nl.tudelft.graphalytics.validation.GraphValues;
import nl.tudelft.graphalytics.validation.io.DoubleParser;
import nl.tudelft.graphalytics.validation.io.GraphParser;
import org.junit.Test;

import java.io.IOException;
import java.util.Scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Framework for validating the output of an implementation of the local clustering coefficient algorithm. Defines two
 * functions to be implemented to run a platform-specific local clustering coefficient implementation on an in-memory
 * graph.
 *
 * @author Tim Hegeman
 */
public abstract class LocalClusteringCoefficientValidationTest {

	/**
	 * Executes the platform-specific implementation of the local clustering coefficient algorithm on an in-memory
	 * directed graph, and returns the output of the execution.
	 * <p/>
	 * This function is called with sample graphs and the output is compared with known-correct results.
	 *
	 * @param graph the graph to execute the local clustering coefficient algorithm on
	 * @return the output of the local clustering coefficient algorithm
	 * @throws Exception
	 */
	public abstract LocalClusteringCoefficientOutput executeDirectedLocalClusteringCoefficient(
			GraphStructure graph) throws Exception;

	/**
	 * Executes the platform-specific implementation of the local clustering coefficient algorithm on an in-memory
	 * undirected graph, and returns the output of the execution.
	 * <p/>
	 * This function is called with sample graphs and the output is compared with known-correct results.
	 *
	 * @param graph the graph to execute the local clustering coefficient algorithm on
	 * @return the output of the local clustering coefficient algorithm
	 * @throws Exception
	 */
	public abstract LocalClusteringCoefficientOutput executeUndirectedLocalClusteringCoefficient(
			GraphStructure graph) throws Exception;

	@Test
	public final void testDirectedLocalClusteringCoefficientOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/stats-dir-input";
		final String graphOutputPath = "/validation-graphs/stats-dir-output-graph";
		final String lccOutputPath = "/validation-graphs/stats-dir-output-lcc";

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), true);

		LocalClusteringCoefficientOutput executionResult = executeDirectedLocalClusteringCoefficient(inputGraph);

		validateLocalClusteringCoefficient(executionResult, graphOutputPath, lccOutputPath);
	}

	@Test
	public final void testUndirectedLocalClusteringCoefficientOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/stats-undir-input";
		final String graphOutputPath = "/validation-graphs/stats-undir-output-graph";
		final String lccOutputPath = "/validation-graphs/stats-undir-output-lcc";

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), false);

		LocalClusteringCoefficientOutput executionResult = executeUndirectedLocalClusteringCoefficient(inputGraph);

		validateLocalClusteringCoefficient(executionResult, graphOutputPath, lccOutputPath);
	}

	/**
	 * Validates the output of a local clustering coefficient implementation. The output is compared with known results
	 * in separate files.
	 *
	 * @param executionResult the result of the breadth-first search execution
	 * @param graphOutputPath the output file to read the correct results from
	 * @param lccOutputPath   the file to read the correct mean local clustering coefficient from
	 * @throws IOException iff the output file could not be loaded
	 */
	private void validateLocalClusteringCoefficient(LocalClusteringCoefficientOutput executionResult,
	                                                String graphOutputPath, String lccOutputPath) throws IOException {
		final double EPSILON = 1e-6;

		GraphValues<Double> outputGraph = GraphParser.parseGraphValuesFromDataset(
				getClass().getResourceAsStream(graphOutputPath), new DoubleParser());
		double expectedMeanLcc = loadExpectedMeanLocalClusteringCoefficient(lccOutputPath);

		assertThat("result graph has the correct number of vertices",
				executionResult.getVertices(), hasSize(outputGraph.getVertices().size()));
		assertThat("result graph has the expected vertex ids",
				executionResult.getVertices(), containsInAnyOrder(outputGraph.getVertices().toArray()));
		for (long vertexId : outputGraph.getVertices()) {
			assertThat("vertex " + vertexId + " has correct value",
					executionResult.getLocalClusteringCoefficientForVertex(vertexId),
					is(closeTo(outputGraph.getVertexValue(vertexId), EPSILON)));
		}

		assertThat("mean local clustering coefficient is correct",
				executionResult.getMeanLocalClusteringCoefficient(), is(closeTo(expectedMeanLcc, EPSILON)));
	}

	/**
	 * Reads the expected mean local clustering coefficient from a file.
	 *
	 * @param lccOutputPath the path of the file containing the mean local clustering coefficient
	 * @return the read mean local clustering coefficient
	 * @throws IOException iff the file could not be read
	 */
	private double loadExpectedMeanLocalClusteringCoefficient(String lccOutputPath) throws IOException {
		try (Scanner scanner = new Scanner(getClass().getResourceAsStream(lccOutputPath))) {
			return scanner.nextDouble();
		}
	}

}
