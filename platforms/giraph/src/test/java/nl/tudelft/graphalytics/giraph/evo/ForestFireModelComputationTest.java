package nl.tudelft.graphalytics.giraph.evo;

import nl.tudelft.graphalytics.giraph.AbstractComputationTest;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Computation;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.utils.TestGraph;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Test case for the forest fire model implementation on Giraph. Due to the randomized nature of this algorithm, this
 * test cases focuses on verifying properties that must hold, regardless of random choices made in the algorithm.
 * This includes:
 * <ul>
 * <li>Vertices in the original graph still exist and did not gain or lose any edges.</li>
 * <li>The appropriate number of new vertices were created.</li>
 * <li>Newly created vertices only have outgoing edges to older vertices.</li>
 * <li>All "burnt" vertices are reachable within the maximum number of iterations, via other "burnt" vertices.</li>
 * </ul>
 *
 * @author Tim Hegeman
 */
public class ForestFireModelComputationTest extends AbstractComputationTest<ForestFireModelData, NullWritable> {

	private static final float P_RATIO = 0.5f;
	private static final float R_RATIO = 0.5f;
	private static final int MAX_ITERATIONS = 2;
	private static final int NUM_VERTICES = 5;
	private static final long MAX_ID = 50;

	private static void verifyOriginalGraphUnchanged(
			TestGraph<LongWritable, ForestFireModelData, NullWritable> original,
			TestGraph<LongWritable, ForestFireModelData, NullWritable> result) {
		assertThat("original vertices still exist",
				original.getVertices().keySet(), everyItem(isIn(result.getVertices().keySet())));
		for (LongWritable vertexId : original.getVertices().keySet()) {
			Set<LongWritable> resultEdges = new HashSet<>();
			for (Edge<LongWritable, NullWritable> edge : result.getVertex(vertexId).getEdges()) {
				resultEdges.add(edge.getTargetVertexId());
			}

			Set<LongWritable> originalEdges = new HashSet<>();
			for (Edge<LongWritable, NullWritable> edge : original.getVertex(vertexId).getEdges()) {
				originalEdges.add(edge.getTargetVertexId());
			}

			assertThat("original vertex " + vertexId.get() + " did not gain or lose any edges",
					resultEdges, is(equalTo(originalEdges)));
		}
	}

	private static void verifyNewVerticesCreated(TestGraph<LongWritable, ForestFireModelData, NullWritable> original,
	                                             TestGraph<LongWritable, ForestFireModelData, NullWritable> result) {
		assertThat("correct number of new vertices were created",
				result.getVertices().keySet(), hasSize(original.getVertices().size() + NUM_VERTICES));
		for (long vertexId = MAX_ID + 1; vertexId <= MAX_ID + NUM_VERTICES; vertexId++) {
			assertThat("vertex with id " + vertexId + " is created",
					result.getVertices().containsKey(new LongWritable(vertexId)));
		}
	}

	private static void verifyNewEdgesAreFeasible(TestGraph<LongWritable, ForestFireModelData, NullWritable> result,
	                                              Vertex<LongWritable, ForestFireModelData, NullWritable> newVertex) {
		for (Edge<LongWritable, NullWritable> edge : newVertex.getEdges()) {
			assertThat("new edges are formed to older vertices",
					edge.getTargetVertexId().get(), is(lessThan(newVertex.getId().get())));
		}

		// Extract a subgraph containing all vertices connected to the new vertex, including edges
		// All edges are treated as undirected because the algorithm can traverse both in and out edges
		Set<Long> connectedVertices = new HashSet<>();
		Map<Long, Set<Long>> edges = new HashMap<>();
		for (Edge<LongWritable, NullWritable> edge : newVertex.getEdges()) {
			connectedVertices.add(edge.getTargetVertexId().get());
			edges.put(edge.getTargetVertexId().get(), new HashSet<Long>());
		}
		for (long vertexId : connectedVertices) {
			for (Edge<LongWritable, NullWritable> edge : result.getVertex(new LongWritable(vertexId)).getEdges()) {
				if (connectedVertices.contains(edge.getTargetVertexId().get())) {
					edges.get(vertexId).add(edge.getTargetVertexId().get());
					edges.get(edge.getTargetVertexId().get()).add(vertexId);
				}
			}
		}

		// Perform a BFS from all vertices to find out if any source vertex exists for which all other vertices are
		// at most MAX_ITERATIONS away
		boolean validSelection = false;
		for (long vertexId : connectedVertices) {
			validSelection = validSelection || maxDistanceWithinLimit(edges, vertexId);
		}
		assertThat("connected vertices are within maximum distance of a potential source", validSelection);
	}

	private static boolean maxDistanceWithinLimit(Map<Long, Set<Long>> edges, long sourceVertex) {
		Set<Long> reachableVertices = new HashSet<>();
		Set<Long> verticesAtCurrentDepth = reachableVertices;
		int currentDepth = 0;
		reachableVertices.add(sourceVertex);

		while (currentDepth < MAX_ITERATIONS) {
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

	@Test
	public void testDirectedExample() throws Exception {
		performTest(DirectedForestFireModelComputation.class, "/test-examples/evo-dir-input");
	}

	@Test
	public void testUndirectedExample() throws Exception {
		performTest(UndirectedForestFireModelComputation.class, "/test-examples/evo-undir-input");
	}

	private void performTest(Class<? extends Computation> computationClass, String input) throws Exception {
		GiraphConfiguration configuration = new GiraphConfiguration();
		configuration.setComputationClass(computationClass);
		configuration.setWorkerContextClass(ForestFireModelWorkerContext.class);
		ForestFireModelConfiguration.AVAILABLE_VERTEX_ID.set(configuration, MAX_ID + 1);
		ForestFireModelConfiguration.BACKWARD_PROBABILITY.set(configuration, R_RATIO);
		ForestFireModelConfiguration.FORWARD_PROBABILITY.set(configuration, P_RATIO);
		ForestFireModelConfiguration.MAX_ITERATIONS.set(configuration, MAX_ITERATIONS);
		ForestFireModelConfiguration.NEW_VERTICES.set(configuration, NUM_VERTICES);

		TestGraph<LongWritable, ForestFireModelData, NullWritable> result = runTest(configuration, input);
		TestGraph<LongWritable, ForestFireModelData, NullWritable> original = parseGraphStructure(configuration, input);

		verifyOriginalGraphUnchanged(original, result);
		verifyNewVerticesCreated(original, result);
		for (long vertexId = MAX_ID + 1; vertexId <= MAX_ID + NUM_VERTICES; vertexId++) {
			verifyNewEdgesAreFeasible(result, result.getVertex(new LongWritable(vertexId)));
		}
	}

	@Override
	protected ForestFireModelData getDefaultValue(long vertexId) {
		return new ForestFireModelData();
	}

	@Override
	protected NullWritable getDefaultEdgeValue(long sourceId, long destinationId) {
		return NullWritable.get();
	}

	@Override
	protected ForestFireModelData parseValue(long vertexId, String value) {
		// Unused
		return null;
	}
}
