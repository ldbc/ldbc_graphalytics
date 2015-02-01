package nl.tudelft.graphalytics.neo4j;

import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Base class for testing Neo4j jobs. This class is responsible for creating and cleaning up test databases, doing
 * transaction management, etc.
 *
 * @author Tim Hegeman
 */
public abstract class AbstractComputationTest {

	protected GraphDatabaseService graphDatabase;
	private Map<Long, Long> vertexToNodeIds;

	@Before
	public void prepareDatabase() {
		graphDatabase = new TestGraphDatabaseFactory().newImpermanentDatabase();
	}

	@After
	public void shutdownDatabase() {
		graphDatabase.shutdown();
	}

	protected long getNodeId(long vertexId) {
		return vertexToNodeIds.get(vertexId);
	}

	protected Node getNode(long vertexId) {
		return graphDatabase.getNodeById(vertexToNodeIds.get(vertexId));
	}

	protected void loadGraph(Collection<Long> vertices, Map<Long, ? extends Collection<Long>> edges) {
		try (Transaction transaction = graphDatabase.beginTx()) {
			Map<Long, Node> nodes = new HashMap<>();
			vertexToNodeIds = new HashMap<>();
			for (long vertexId : vertices) {
				nodes.put(vertexId, graphDatabase.createNode());
				vertexToNodeIds.put(vertexId, nodes.get(vertexId).getId());
			}

			for (long sourceId : edges.keySet()) {
				for (long destinationId : edges.get(sourceId)) {
					nodes.get(sourceId).createRelationshipTo(nodes.get(destinationId), Neo4jConfiguration.EDGE);
				}
			}
			transaction.success();
		}
	}

	protected void loadGraphFromResource(String resourceName) throws IOException {
		Map<Long, Set<Long>> edges = new HashMap<>();

		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream(resourceName)))) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				parseGraphLineToEdges(line, edges);
			}
		}

		loadGraph(edges.keySet(), edges);
	}

	private static void parseGraphLineToEdges(String line, Map<Long, Set<Long>> edges) {
		String[] tokens = line.split(" ");
		Long sourceId = Long.parseLong(tokens[0]);
		addVertex(sourceId, edges);
		for (int i = 1; i < tokens.length; i++) {
			Long destinationId = Long.parseLong(tokens[i]);
			addEdge(sourceId, destinationId, edges);
		}
	}

	private static void addEdge(long sourceId, long destinationId, Map<Long, Set<Long>> edges) {
		addVertex(destinationId, edges);
		edges.get(sourceId).add(destinationId);
	}

	private static void addVertex(long vertexId, Map<Long, Set<Long>> edges) {
		if (!edges.containsKey(vertexId)) {
			edges.put(vertexId, new HashSet<Long>());
		}
	}

}
