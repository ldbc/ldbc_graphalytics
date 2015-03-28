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
 * @author Tim Hegeman
 */
public abstract class LocalClusteringCoefficientValidationTest {

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
					executionResult.getComponentIdForVertex(vertexId),
					is(closeTo(outputGraph.getVertexValue(vertexId), EPSILON)));
		}

		assertThat("mean local clustering coefficient is correct",
				executionResult.getMeanLocalClusteringCoefficient(), is(closeTo(expectedMeanLcc, EPSILON)));
	}

	private double loadExpectedMeanLocalClusteringCoefficient(String lccOutputPath) throws IOException {
		try (Scanner scanner = new Scanner(getClass().getResourceAsStream(lccOutputPath))) {
			return scanner.nextDouble();
		}
	}

	public abstract LocalClusteringCoefficientOutput executeDirectedLocalClusteringCoefficient(
			GraphStructure graph) throws Exception;

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

}
