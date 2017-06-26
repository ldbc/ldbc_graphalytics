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
package science.atlarge.graphalytics.validation.algorithms.pr;

import science.atlarge.graphalytics.domain.algorithms.PageRankParameters;
import science.atlarge.graphalytics.validation.GraphStructure;
import science.atlarge.graphalytics.validation.GraphValues;
import science.atlarge.graphalytics.validation.io.DoubleParser;
import science.atlarge.graphalytics.validation.io.GraphParser;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Framework for validating the output of an implementation of the PageRank algorithm. Defines two functions to be
 * implemented to run a platform-specific PageRank implementation on an in-memory graph.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public abstract class PageRankValidationTest {

	/**
	 * Executes the platform-specific implementation of the PageRank algorithm on an in-memory directed graph with given
	 * parameters, and returns the output of the execution.
	 * <p/>
	 * This function is called with sample graphs and the output is compared with known-correct results.
	 *
	 * @param graph      the graph to execute the PageRank algorithm on
	 * @param parameters the values for the algorithm parameters to use
	 * @return the output of the PageRank algorithm
	 * @throws Exception
	 */
	public abstract PageRankOutput executeDirectedPageRank(GraphStructure graph, PageRankParameters parameters)
			throws Exception;

	/**
	 * Executes the platform-specific implementation of the PageRank algorithm on an in-memory undirected graph with
	 * given parameters, and returns the output of the execution.
	 * <p/>
	 * This function is called with sample graphs and the output is compared with known-correct results.
	 *
	 * @param graph      the graph to execute the PageRank algorithm on
	 * @param parameters the values for the algorithm parameters to use
	 * @return the output of the PageRank algorithm
	 * @throws Exception
	 */
	public abstract PageRankOutput executeUndirectedPageRank(GraphStructure graph, PageRankParameters parameters)
			throws Exception;

	@Test
	public final void testDirectedPageRankOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/pr/dir-input";
		final String outputPath = "/validation-graphs/pr/dir-output";
		final float dampingFactor = 0.85f;
		final int numberOfIterations = 14;

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), true);

		PageRankParameters parameters = new PageRankParameters(dampingFactor, numberOfIterations);
		PageRankOutput executionResult = executeDirectedPageRank(inputGraph, parameters);

		validatePageRank(executionResult, outputPath);
	}

	@Test
	public final void testUndirectedPageRankOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/pr/undir-input";
		final String outputPath = "/validation-graphs/pr/undir-output";
		final float dampingFactor = 0.85f;
		final int numberOfIterations = 26;

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), true);

		PageRankParameters parameters = new PageRankParameters(dampingFactor, numberOfIterations);
		PageRankOutput executionResult = executeUndirectedPageRank(inputGraph, parameters);

		validatePageRank(executionResult, outputPath);
	}

	private void validatePageRank(PageRankOutput executionResult, String outputPath) throws IOException {
		final double EPSILON = 1e-4;

		GraphValues<Double> outputGraph = GraphParser.parseGraphValuesFromDataset(
				getClass().getResourceAsStream(outputPath), new DoubleParser());

		assertThat("result graph has the correct number of vertices",
				executionResult.getVertices(), hasSize(outputGraph.getVertices().size()));
		assertThat("result graph has the expected vertex ids",
				executionResult.getVertices(), containsInAnyOrder(outputGraph.getVertices().toArray()));
		for (long vertexId : outputGraph.getVertices()) {
			double expectedValue = outputGraph.getVertexValue(vertexId);
			double actualValue = executionResult.getRankForVertex(vertexId);
			double deviation = (actualValue - expectedValue) / expectedValue;
			assertThat("vertex " + vertexId + " has correct value",
					deviation, is(closeTo(0.0, EPSILON)));
		}
	}

}
