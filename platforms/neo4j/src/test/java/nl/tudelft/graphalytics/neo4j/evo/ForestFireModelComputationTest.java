package nl.tudelft.graphalytics.neo4j.evo;

import nl.tudelft.graphalytics.domain.algorithms.ForestFireModelParameters;
import nl.tudelft.graphalytics.neo4j.AbstractComputationTest;
import nl.tudelft.graphalytics.neo4j.Neo4jConfiguration;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.tooling.GlobalGraphOperations;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Test case for the forest fire model implementation on Neo4j. Due to the randomized nature of this algorithm, this
 * test cases focuses on verifying properties that must hold, regardless of random choices made in the algorithm.
 * This includes:
 * <ul>
 *     <li>Vertices in the original graph still exist and did not gain or lose any edges.</li>
 *     <li>The appropriate number of new vertices were created.</li>
 *     <li>Newly created vertices only have outgoing edges to older vertices.</li>
 *     <li>All "burnt" vertices are reachable within the maximum number of iterations, via other "burnt" vertices.</li>
 * </ul>
 *
 * @author Tim Hegeman
 */
public class ForestFireModelComputationTest extends AbstractComputationTest {

	private static void assertThatVertexHasFeasibleEdges(long vertexId, long initialNeighbour,
	                                                     Map<Long, Set<Long>> graph, int maxIterations) {
		// Check that edges were created only to previously existing vertices
		for (long destinationId : graph.get(vertexId)) {
			assertThat("edges were formed to older vertices", destinationId, lessThan(vertexId));
		}
		assertThat("edge to initial vertex exists", initialNeighbour, isIn(graph.get(vertexId)));

		Map<Long, Set<Long>> incomingEdges = findIncomingEdges(graph);
		Set<Long> neighbours = graph.get(vertexId);
		Set<Long> reachableVertices = new HashSet<>();
		Set<Long> verticesAtCurrentDepth = reachableVertices;
		int currentDepth = 0;
		reachableVertices.add(initialNeighbour);

		// Perform a BFS up to the specified depth, keeping only vertices that are neighbours of the newly created
		// vertex using set operations
		while (currentDepth < maxIterations) {
			Set<Long> verticesAtNextDepth = new HashSet<>();

			for (long reachableVertex : verticesAtCurrentDepth) {
				verticesAtNextDepth.addAll(graph.get(reachableVertex));
				verticesAtNextDepth.addAll(incomingEdges.get(reachableVertex));
			}

			verticesAtNextDepth.removeAll(reachableVertices);
			verticesAtNextDepth.retainAll(neighbours);
			reachableVertices.addAll(verticesAtNextDepth);
			verticesAtCurrentDepth = verticesAtNextDepth;
			currentDepth++;
		}

		assertThat("all neighbours of new vertex are reachable",
				neighbours, everyItem(isIn(reachableVertices)));
	}

	private static Map<Long, Set<Long>> findIncomingEdges(Map<Long, Set<Long>> graph) {
		Map<Long, Set<Long>> incomingEdges = new HashMap<>();
		for (long vertexId : graph.keySet()) {
			incomingEdges.put(vertexId, new HashSet<Long>());
		}
		for (long sourceId : graph.keySet()) {
			for (long destinationId : graph.get(sourceId)) {
				incomingEdges.get(destinationId).add(sourceId);
			}
		}
		return incomingEdges;
	}

	private static long getMaxId(Map<Long, ?> graph) {
		long max = Long.MIN_VALUE;
		for (long id : graph.keySet()) {
			if (id > max) {
				max = id;
			}
		}
		return max;
	}

	private Map<Long, Set<Long>> captureGraphState() {
		try (Transaction ignored = graphDatabase.beginTx()) {
			Map<Long, Set<Long>> graphState = new HashMap<>();
			for (Node node : GlobalGraphOperations.at(graphDatabase).getAllNodes()) {
				long vertexId = (long) node.getProperty(Neo4jConfiguration.ID_PROPERTY);
				graphState.put(vertexId, new HashSet<Long>());
				for (Relationship edge : node.getRelationships(Neo4jConfiguration.EDGE, Direction.OUTGOING)) {
					graphState.get(vertexId).add((long) edge.getEndNode().getProperty(Neo4jConfiguration.ID_PROPERTY));
				}
			}
			return graphState;
		}
	}

	private long getInitialNeighbour(long vertexId) {
		try (Transaction ignored = graphDatabase.beginTx()) {
			ResourceIterable<Node> matches = graphDatabase.findNodesByLabelAndProperty(
					null, Neo4jConfiguration.ID_PROPERTY, vertexId);
			Node vertex = null;
			for (Node match : matches)
				vertex = match;
			return (long) vertex.getProperty(ForestFireModelComputation.INITIAL_VERTEX);
		}
	}

	@Test
	public void testExample() throws IOException {
		final float pRatio = 0.5f;
		final float rRatio = 0.5f;
		final int maxIterations = 2;
		final int numVertices = 5;

		// Load data
		loadGraphFromResource("/test-examples/evo-input");
		// Capture graph state
		Map<Long, Set<Long>> initialGraph = captureGraphState();
		long maxId = getMaxId(initialGraph);
		// Execute forest fire model algorithm
		new ForestFireModelComputation(graphDatabase, new ForestFireModelParameters(
				maxId, pRatio, rRatio, maxIterations, numVertices)).run();
		// Capture updated graph state
		Map<Long, Set<Long>> evolvedGraph = captureGraphState();

		// Assert properties that must hold, regardless of randomness:
		assertThat("old vertices still exist", initialGraph.keySet(), everyItem(isIn(evolvedGraph.keySet())));
		for (long oldVertex : initialGraph.keySet()) {
			assertThat("old vertex did not gain or lose outgoing edges",
					evolvedGraph.get(oldVertex), is(equalTo(initialGraph.get(oldVertex))));
		}
		assertThat("new vertices were created", evolvedGraph.keySet(), hasSize(initialGraph.size() + numVertices));
		for (long newVertexId = maxId + 1; newVertexId <= maxId + numVertices; newVertexId++) {
			assertThat("new vertices have consecutive ids", evolvedGraph.keySet(), hasItem(newVertexId));
		}

		// Verify that the edges created for each new vertex are feasible
		for (long newVertexId = maxId + 1; newVertexId <= maxId + numVertices; newVertexId++) {
			assertThatVertexHasFeasibleEdges(newVertexId, getInitialNeighbour(newVertexId),
					evolvedGraph, maxIterations);
		}
	}

}
