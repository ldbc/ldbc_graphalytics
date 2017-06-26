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
package science.atlarge.graphalytics.validation.algorithms.wcc;

import science.atlarge.graphalytics.validation.GraphStructure;
import science.atlarge.graphalytics.validation.GraphValues;
import science.atlarge.graphalytics.validation.io.GraphParser;
import science.atlarge.graphalytics.validation.io.LongParser;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Framework for validating the output of an implementation of weakly connected components. Defines two functions
 * to be implemented to run a platform-specific connected components implementation on an in-memory graph.
 *
 * @author Mihai Capotă
 * @author Stijn Heldens
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public abstract class WeaklyConnectedComponentsValidationTest {

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
	public abstract WeaklyConnectedComponentsOutput executeDirectedConnectedComponents(
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
	public abstract WeaklyConnectedComponentsOutput executeUndirectedConnectedComponents(
			GraphStructure graph) throws Exception;

	@Test
	public final void testDirectedConnectedComponentsOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/wcc/dir-input";
		final String outputPath = "/validation-graphs/wcc/dir-output";

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), true);

		WeaklyConnectedComponentsOutput executionResult = executeDirectedConnectedComponents(inputGraph);

		validateConnectedComponents(executionResult, outputPath);
	}

	@Test
	public final void testUndirectedConnectedComponentsOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/wcc/undir-input";
		final String outputPath = "/validation-graphs/wcc/undir-output";

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), false);

		WeaklyConnectedComponentsOutput executionResult = executeUndirectedConnectedComponents(inputGraph);

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
	private void validateConnectedComponents(WeaklyConnectedComponentsOutput executionResult, String outputPath)
			throws IOException {
		GraphValues<Long> outputGraph = GraphParser.parseGraphValuesFromDataset(
				getClass().getResourceAsStream(outputPath), new LongParser());

		assertThat("result graph has the correct number of vertices",
				executionResult.getVertices(), hasSize(outputGraph.getVertices().size()));
		assertThat("result graph has the expected vertex ids",
				executionResult.getVertices(), containsInAnyOrder(outputGraph.getVertices().toArray()));

		Map<Long, Long> actual2expect = new HashMap<>();
		Map<Long, Long> expect2actual = new HashMap<>();

		for (long vertexId : outputGraph.getVertices()) {
			long expect = outputGraph.getVertexValue(vertexId);
			long actual = executionResult.getComponentIdForVertex(vertexId);

			if (!actual2expect.containsKey(expect)) {
				actual2expect.put(expect, actual);
			}

			if (!expect2actual.containsKey(actual)) {
				expect2actual.put(actual, expect);
			}

			assertThat("vertex " + vertexId + " has correct value",
					expect2actual.get(actual) == expect && actual2expect.get(expect) == actual);
		}
	}

}
