/*
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
package nl.tudelft.graphalytics.validation.conn;

import nl.tudelft.graphalytics.validation.GraphStructure;
import nl.tudelft.graphalytics.validation.GraphValues;
import nl.tudelft.graphalytics.validation.io.GraphParser;
import nl.tudelft.graphalytics.validation.io.LongParser;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Framework for validating the output of an implementation of the connected components algorithm. Defines two functions
 * to be implemented to run a platform-specific connected components implementation on an in-memory graph.
 *
 * @author Tim Hegeman
 */
public abstract class ConnectedComponentsValidationTest {

	/**
	 * Executes the platform-specific implementation of the connected components algorithm on an in-memory directed
	 * graph, and returns the output of the execution.
	 * <p/>
	 * This function is called with sample graphs and the output is compared with known-correct results.
	 *
	 * @param graph the graph to execute the connected components algorithm on
	 * @return the output of the connected components algorithm
	 * @throws Exception
	 */
	public abstract ConnectedComponentsOutput executeDirectedConnectedComponents(
			GraphStructure graph) throws Exception;

	/**
	 * Executes the platform-specific implementation of the connected components algorithm on an in-memory undirected
	 * graph, and returns the output of the execution.
	 * <p/>
	 * This function is called with sample graphs and the output is compared with known-correct results.
	 *
	 * @param graph the graph to execute the connected components algorithm on
	 * @return the output of the connected components algorithm
	 * @throws Exception
	 */
	public abstract ConnectedComponentsOutput executeUndirectedConnectedComponents(
			GraphStructure graph) throws Exception;

	@Test
	public final void testDirectedConnectedComponentsOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/conn-dir-input";
		final String outputPath = "/validation-graphs/conn-dir-output";

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), true);

		ConnectedComponentsOutput executionResult = executeDirectedConnectedComponents(inputGraph);

		validateConnectedComponents(executionResult, outputPath);
	}

	@Test
	public final void testUndirectedConnectedComponentsOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/conn-undir-input";
		final String outputPath = "/validation-graphs/conn-undir-output";

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), false);

		ConnectedComponentsOutput executionResult = executeUndirectedConnectedComponents(inputGraph);

		validateConnectedComponents(executionResult, outputPath);
	}

	/**
	 * Validates the output of a connected components implementation. The output is compared with known results in a
	 * separate file.
	 *
	 * @param executionResult the result of the breadth-first search execution
	 * @param outputPath      the output file to read the correct results from
	 * @throws IOException iff the output file could not be loaded
	 */
	private void validateConnectedComponents(ConnectedComponentsOutput executionResult, String outputPath)
			throws IOException {
		GraphValues<Long> outputGraph = GraphParser.parseGraphValuesFromDataset(
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
