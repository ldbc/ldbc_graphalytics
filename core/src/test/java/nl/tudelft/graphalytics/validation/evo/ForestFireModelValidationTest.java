package nl.tudelft.graphalytics.validation.evo;

import nl.tudelft.graphalytics.domain.algorithms.ForestFireModelParameters;
import nl.tudelft.graphalytics.validation.GraphStructure;
import nl.tudelft.graphalytics.validation.io.GraphParser;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Tim Hegeman
 */
public abstract class ForestFireModelValidationTest {

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
								edges.get(destinationId), contains(sourceId));
						edges.get(destinationId).remove(sourceId);
					}
				}
			}
		}

		return new GraphStructure(edges);
	}

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

	private static void verifyNewVerticesCreated(GraphStructure original, GraphStructure result,
	                                             long maximumOriginalId, int numberOfNewVertices) {
		assertThat("correct number of new vertices were created",
				result.getVertices(), hasSize(original.getVertices().size() + numberOfNewVertices));
		for (long vertexId = maximumOriginalId + 1; vertexId <= maximumOriginalId + numberOfNewVertices; vertexId++) {
			assertThat("vertex with id " + vertexId + " is created",
					vertexId, isIn(result.getVertices()));
		}
	}

	private static void verifyNewEdgesAreFeasible(GraphStructure original, GraphStructure result,
	                                              long newVertexId, int maximumDistance) {
		for (long destinationId : result.getEdgesForVertex(newVertexId)) {
			assertThat("new edges are formed to older vertices",
					destinationId, is(lessThan(newVertexId)));
		}

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
			validSelection = validSelection || maxDistanceWithinLimit(edges, vertexId, maximumDistance);
		}
		assertThat("connected vertices are within maximum distance of a potential source", validSelection);
	}

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

	public abstract GraphStructure executeDirectedForestFireModel(
			GraphStructure graph, ForestFireModelParameters parameters) throws Exception;

	public abstract GraphStructure executeUndirectedForestFireModel(
			GraphStructure graph, ForestFireModelParameters parameters) throws Exception;

	@Test
	public final void testDirectedForestFireModelOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/stats-dir-input";
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
		final String inputPath = "/validation-graphs/stats-undir-input";
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
