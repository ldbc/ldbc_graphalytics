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
package science.atlarge.graphalytics.validation.algorithms.ffm;

import science.atlarge.graphalytics.domain.algorithms.ForestFireModelParameters;
import science.atlarge.graphalytics.validation.GraphStructure;
import science.atlarge.graphalytics.validation.io.GraphParser;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Framework for validating the output of an implementation of the forest fire model algorithm. Defines two functions
 * to be implemented to run a platform-specific forest fire model implementation on an in-memory graph. Unlike other
 * algorithms in the Graphalytics benchmark, this algorithm has random output. This class tests that the output of an
 * implementation is feasible, but may not identify an incorrect implementation as such.
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public abstract class ForestFireModelValidationTest {

	/**
	 * Turns newly-added edges from an undirected graph into directed edges by removing the edges in the direction of
	 * new vertices. This results in a directed graph that could have been the result of executing the directed forest
	 * fire model algorithm on the input graph. As a result, the corrected undirected graph can be validated as a
	 * directed graph, reducing code duplication.
	 *
	 * @param result            the output of the undirected forest fire model algorithm
	 * @param maximumOriginalId the largest vertex id in the input graph
	 * @return the correct graph
	 */
	private static GraphStructure deduplicateNewUndirectedEdges(GraphStructure result, long maximumOriginalId) {
		Map<Long, Set<Long>> edges = new HashMap<>();
		for (long vertexId : result.getVertices()) {
			edges.put(vertexId, new HashSet<>(result.getEdgesForVertex(vertexId)));
		}

		for (long sourceId : result.getVertices()) {
			if (sourceId > maximumOriginalId) {
				for (long destinationId : result.getEdgesForVertex(sourceId)) {
					if (destinationId < sourceId) {
						assertThat("edge from " + sourceId + " to " + destinationId + " is undirected",
								sourceId, isIn(edges.get(destinationId)));
						edges.get(destinationId).remove(sourceId);
					}
				}
			}
		}

		return new GraphStructure(edges);
	}

	/**
	 * Verifies that the vertices in the input graph still exist in the output, and that no edges were added or removed
	 * between these vertices.
	 *
	 * @param original the input graph
	 * @param result   the output graph
	 */
	private static void verifyOriginalGraphUnchanged(GraphStructure original, GraphStructure result) {
		assertThat("original vertices still exist",
				original.getVertices(), everyItem(isIn(result.getVertices())));
		for (long vertexId : original.getVertices()) {
			Set<Long> originalEdges = result.getEdgesForVertex(vertexId);
			Set<Long> resultEdges = result.getEdgesForVertex(vertexId);

			assertThat("original vertex " + vertexId + " did not gain or lose any edges",
					resultEdges, is(equalTo(originalEdges)));
		}
	}

	/**
	 * Verifies that the correct number of new vertices were created and that they have the expected vertex ids.
	 *
	 * @param original            the input graph
	 * @param result              the output graph
	 * @param maximumOriginalId   the highest vertex id in the input graph
	 * @param numberOfNewVertices the number of vertices that should have been created
	 */
	private static void verifyNewVerticesCreated(GraphStructure original, GraphStructure result,
	                                             long maximumOriginalId, int numberOfNewVertices) {
		assertThat("correct number of new vertices were created",
				result.getVertices(), hasSize(original.getVertices().size() + numberOfNewVertices));
		for (long vertexId = maximumOriginalId + 1; vertexId <= maximumOriginalId + numberOfNewVertices; vertexId++) {
			assertThat("vertex with id " + vertexId + " is created",
					vertexId, isIn(result.getVertices()));
		}
	}

	/**
	 * Verifies that the outgoing edges of a specific new vertex are a feasible result of the forest fire model
	 * algorithm. The forest fire model algorithm adds a single edge from the new vertex to a random existing vertex.
	 * Afterwards, additional edges are added to vertices that are connected (via either incoming or outgoing edges)
	 * to vertices that are already connected to the new vertex. This function checks that all vertices connected to
	 * the new vertex in the output graph form a weakly connected component, and that within this component there exists
	 * a vertex such that all other vertices in the component are reachable within the number of iterations executed
	 * by the algorithm.
	 *
	 * @param original           the input graph
	 * @param result             the output graph
	 * @param newVertexId        the id of the vertex to validate the edges for
	 * @param numberOfIterations the number of iterations executed by the forest fire model algorithm
	 */
	private static void verifyNewEdgesAreFeasible(GraphStructure original, GraphStructure result,
	                                              long newVertexId, int numberOfIterations) {
		for (long destinationId : result.getEdgesForVertex(newVertexId)) {
			assertThat("new edges are formed to older vertices",
					destinationId, is(lessThan(newVertexId)));
		}

		assertThat("new vertex with id " + newVertexId + " is connected to at least one vertex",
				result.getEdgesForVertex(newVertexId), is(not(empty())));

		// Extract a subgraph containing all vertices connected to the new vertex, including edges
		// All edges are treated as undirected because the algorithm can traverse both in and out edges
		Set<Long> connectedVertices = new HashSet<>();
		Map<Long, Set<Long>> edges = new HashMap<>();
		for (long destinationId : result.getEdgesForVertex(newVertexId)) {
			connectedVertices.add(destinationId);
			edges.put(destinationId, new HashSet<Long>());
		}
		for (long sourceVertex : connectedVertices) {
			for (long destinationVertex : result.getEdgesForVertex(sourceVertex)) {
				if (connectedVertices.contains(destinationVertex)) {
					edges.get(sourceVertex).add(destinationVertex);
					edges.get(destinationVertex).add(sourceVertex);
				}
			}
		}

		// Perform a BFS from all vertices to find out if any source vertex exists for which all other vertices are
		// at most the given maximum distance away
		boolean validSelection = false;
		for (long vertexId : connectedVertices) {
			validSelection = validSelection || maxDistanceWithinLimit(edges, vertexId, numberOfIterations);
		}
		assertThat("connected vertices are within maximum distance of a potential source", validSelection);
	}

	/**
	 * Checks for a set of vertices and edges and a source vertex that all vertices are reachable with a given maximum
	 * distance. A breadth-first search with limited depth is used to check if all vertices are reachable.
	 *
	 * @param edges           a map of vertices and corresponding outgoing edges
	 * @param sourceVertex    the source vertex from which to compute the distances
	 * @param maximumDistance the maximum distance from the source vertex to search
	 * @return true iff all vertices are reachable within the specified maximum distance
	 */
	private static boolean maxDistanceWithinLimit(Map<Long, Set<Long>> edges, long sourceVertex, int maximumDistance) {
		Set<Long> reachableVertices = new HashSet<>();
		Set<Long> verticesAtCurrentDepth = reachableVertices;
		int currentDepth = 0;
		reachableVertices.add(sourceVertex);

		while (currentDepth < maximumDistance) {
			Set<Long> verticesAtNextDepth = new HashSet<>();

			for (long reachableVertex : verticesAtCurrentDepth) {
				verticesAtNextDepth.addAll(edges.get(reachableVertex));
			}

			verticesAtNextDepth.removeAll(reachableVertices);
			reachableVertices.addAll(verticesAtNextDepth);
			verticesAtCurrentDepth = verticesAtNextDepth;
			currentDepth++;
		}

		return reachableVertices.size() == edges.keySet().size();
	}

	/**
	 * Executes the platform-specific implementation of the forest fire model algorithm on an in-memory directed
	 * graph with given parameters, and returns the output of the execution.
	 * <p/>
	 * This function is called with sample graphs and the output is checked against a number of known invariants.
	 *
	 * @param graph      the graph to execute the forest fire model algorithm on
	 * @param parameters the values for the algorithm parameters to use
	 * @return the output of the forest fire model algorithm
	 * @throws Exception
	 */
	public abstract GraphStructure executeDirectedForestFireModel(
			GraphStructure graph, ForestFireModelParameters parameters) throws Exception;

	/**
	 * Executes the platform-specific implementation of the forest fire model algorithm on an in-memory undirected
	 * graph with given parameters, and returns the output of the execution.
	 * <p/>
	 * This function is called with sample graphs and the output is checked against a number of known invariants.
	 *
	 * @param graph      the graph to execute the forest fire model algorithm on
	 * @param parameters the values for the algorithm parameters to use
	 * @return the output of the forest fire model algorithm
	 * @throws Exception
	 */
	public abstract GraphStructure executeUndirectedForestFireModel(
			GraphStructure graph, ForestFireModelParameters parameters) throws Exception;

	@Test
	public final void testDirectedForestFireModelOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/ffm/dir-input";
		final long maximumVertexId = 50;
		final float pRatio = 0.5f;
		final float rRatio = 0.5f;
		final int numberOfIterations = 2;
		final int numberOfNewVertices = 5;

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), true);

		ForestFireModelParameters parameters = new ForestFireModelParameters(maximumVertexId, pRatio, rRatio,
				numberOfIterations, numberOfNewVertices);
		GraphStructure executionResult = executeDirectedForestFireModel(inputGraph, parameters);

		validateForestFireModel(inputGraph, executionResult, maximumVertexId, numberOfNewVertices, numberOfIterations,
				true);
	}

	@Test
	public final void testUndirectedForestFireModelOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/ffm/undir-input";
		final long maximumVertexId = 50;
		final float pRatio = 0.5f;
		final float rRatio = 0.5f;
		final int numberOfIterations = 2;
		final int numberOfNewVertices = 5;

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), true);

		ForestFireModelParameters parameters = new ForestFireModelParameters(maximumVertexId, pRatio, rRatio,
				numberOfIterations, numberOfNewVertices);
		GraphStructure executionResult = executeUndirectedForestFireModel(inputGraph, parameters);

		validateForestFireModel(inputGraph, executionResult, maximumVertexId, numberOfNewVertices, numberOfIterations,
				false);
	}

	/**
	 * Validates the output of a forest fire model implementation.
	 *
	 * @param inputGraph          the input graph
	 * @param outputGraph         the output if the forest fire model implementation
	 * @param maximumOriginalId   the largest vertex id in the input graph
	 * @param numberOfNewVertices the number of new vertices that should have been added
	 * @param numberOfIterations  the number of iterations for which the algorithm should have been run
	 * @param graphIsDirected     true iff the input graph is directed
	 */
	private void validateForestFireModel(GraphStructure inputGraph, GraphStructure outputGraph, long maximumOriginalId,
	                                     int numberOfNewVertices, int numberOfIterations, boolean graphIsDirected) {
		if (!graphIsDirected) {
			outputGraph = deduplicateNewUndirectedEdges(outputGraph, maximumOriginalId);
		}

		verifyOriginalGraphUnchanged(inputGraph, outputGraph);

		verifyNewVerticesCreated(inputGraph, outputGraph, maximumOriginalId, numberOfNewVertices);

		long firstNewVertexId = maximumOriginalId + 1;
		long lastNewVertexId = maximumOriginalId + numberOfNewVertices;
		for (long newVertexId = firstNewVertexId; newVertexId <= lastNewVertexId; newVertexId++) {
			verifyNewEdgesAreFeasible(inputGraph, outputGraph, newVertexId, numberOfIterations);
		}
	}

}
