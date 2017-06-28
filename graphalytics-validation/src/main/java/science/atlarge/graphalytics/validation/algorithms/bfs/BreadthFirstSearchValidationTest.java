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
package science.atlarge.graphalytics.validation.algorithms.bfs;

import science.atlarge.graphalytics.domain.algorithms.BreadthFirstSearchParameters;
import science.atlarge.graphalytics.validation.GraphStructure;
import science.atlarge.graphalytics.validation.GraphValues;
import science.atlarge.graphalytics.validation.io.GraphParser;
import science.atlarge.graphalytics.validation.io.LongParser;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Framework for validating the output of an implementation of the breadth-first search algorithm. Defines two functions
 * to be implemented to run a platform-specific breadth-first search implementation on an in-memory graph.
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public abstract class BreadthFirstSearchValidationTest {

	/**
	 * Executes the platform-specific implementation of the breadth-first search algorithm on an in-memory directed
	 * graph with given parameters, and returns the output of the execution.
	 * <p/>
	 * This function is called with sample graphs and the output is compared with known-correct results.
	 *
	 * @param graph      the graph to execute the breadth-search search algorithm on
	 * @param parameters the values for the algorithm parameters to use
	 * @return the output of the breadth-first search algorithm
	 * @throws Exception
	 */
	public abstract BreadthFirstSearchOutput executeDirectedBreadthFirstSearch(
			GraphStructure graph, BreadthFirstSearchParameters parameters) throws Exception;

	/**
	 * Executes the platform-specific implementation of the breadth-first search algorithm on an in-memory undirected
	 * graph with given parameters, and returns the output of the execution.
	 * <p/>
	 * This function is called with sample graphs and the output is compared with known-correct results.
	 *
	 * @param graph      the graph to execute the breadth-search search algorithm on
	 * @param parameters the values for the algorithm parameters to use
	 * @return the output of the breadth-first search algorithm
	 * @throws Exception
	 */
	public abstract BreadthFirstSearchOutput executeUndirectedBreadthFirstSearch(
			GraphStructure graph, BreadthFirstSearchParameters parameters) throws Exception;

	@Test
	public final void testDirectedBreadthFirstSearchOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/bfs/dir-input";
		final String outputPath = "/validation-graphs/bfs/dir-output";
		final long sourceVertex = 1L;

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), true);

		BreadthFirstSearchParameters parameters = new BreadthFirstSearchParameters(sourceVertex);
		BreadthFirstSearchOutput executionResult = executeDirectedBreadthFirstSearch(inputGraph, parameters);

		validateBreadthFirstSearch(executionResult, outputPath);
	}

	@Test
	public final void testUndirectedBreadthFirstSearchOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/bfs/undir-input";
		final String outputPath = "/validation-graphs/bfs/undir-output";
		final long sourceVertex = 1L;

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), true);

		BreadthFirstSearchParameters parameters = new BreadthFirstSearchParameters(sourceVertex);
		BreadthFirstSearchOutput executionResult = executeUndirectedBreadthFirstSearch(inputGraph, parameters);

		validateBreadthFirstSearch(executionResult, outputPath);
	}

	/**
	 * Validates the output of a breadth-first search implementation. The output is compared with known results in a
	 * separate file.
	 *
	 * @param executionResult the result of the breadth-first search execution
	 * @param outputPath      the output file to read the correct results from
	 * @throws IOException iff the output file could not be loaded
	 */
	private void validateBreadthFirstSearch(BreadthFirstSearchOutput executionResult, String outputPath)
			throws IOException {
		GraphValues<Long> outputGraph = GraphParser.parseGraphValuesFromDataset(
				getClass().getResourceAsStream(outputPath), new LongParser());

		assertThat("result graph has the correct number of vertices",
				executionResult.getVertices(), hasSize(outputGraph.getVertices().size()));
		assertThat("result graph has the expected vertex ids",
				executionResult.getVertices(), containsInAnyOrder(outputGraph.getVertices().toArray()));
		for (long vertexId : outputGraph.getVertices()) {
			assertThat("vertex " + vertexId + " has correct value",
					executionResult.getPathLengthForVertex(vertexId),
					is(equalTo(outputGraph.getVertexValue(vertexId))));
		}
	}

}
