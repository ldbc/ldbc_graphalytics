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
package science.atlarge.graphalytics.validation.algorithms.lcc;

import science.atlarge.graphalytics.validation.GraphStructure;
import science.atlarge.graphalytics.validation.GraphValues;
import science.atlarge.graphalytics.validation.io.DoubleParser;
import science.atlarge.graphalytics.validation.io.GraphParser;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Framework for validating the output of an implementation of the local clustering coefficient algorithm. Defines two
 * functions to be implemented to run a platform-specific local clustering coefficient implementation on an in-memory
 * graph.
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
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
		final String inputPath = "/validation-graphs/lcc/dir-input";
		final String graphOutputPath = "/validation-graphs/lcc/dir-output";

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), true);

		LocalClusteringCoefficientOutput executionResult = executeDirectedLocalClusteringCoefficient(inputGraph);

		validateLocalClusteringCoefficient(executionResult, graphOutputPath);
	}

	@Test
	public final void testUndirectedLocalClusteringCoefficientOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/lcc/undir-input";
		final String graphOutputPath = "/validation-graphs/lcc/undir-output";

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), false);

		LocalClusteringCoefficientOutput executionResult = executeUndirectedLocalClusteringCoefficient(inputGraph);

		validateLocalClusteringCoefficient(executionResult, graphOutputPath);
	}

	/**
	 * Validates the output of a local clustering coefficient implementation. The output is compared with known results
	 * in separate files.
	 *
	 * @param executionResult the result of the breadth-first search execution
	 * @param graphOutputPath the output file to read the correct results from
	 * @throws IOException iff the output file could not be loaded
	 */
	private void validateLocalClusteringCoefficient(LocalClusteringCoefficientOutput executionResult,
			String graphOutputPath) throws IOException {
		final double EPSILON = 1e-6;

		GraphValues<Double> outputGraph = GraphParser.parseGraphValuesFromDataset(
				getClass().getResourceAsStream(graphOutputPath), new DoubleParser());

		assertThat("result graph has the correct number of vertices",
				executionResult.getVertices(), hasSize(outputGraph.getVertices().size()));
		assertThat("result graph has the expected vertex ids",
				executionResult.getVertices(), containsInAnyOrder(outputGraph.getVertices().toArray()));
		for (long vertexId : outputGraph.getVertices()) {
			assertThat("vertex " + vertexId + " has correct value",
					executionResult.getLocalClusteringCoefficientForVertex(vertexId),
					is(closeTo(outputGraph.getVertexValue(vertexId), EPSILON)));
		}
	}

}
