package nl.tudelft.graphalytics.neo4j.evo;

import nl.tudelft.graphalytics.domain.algorithms.ForestFireModelParameters;
import nl.tudelft.graphalytics.neo4j.Neo4jConfiguration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.Random;

/**
 * Implementation of the forest fire model for graph evolution using Neo4j.
 *
 * @author Tim Hegeman
 */
public class ForestFireModelComputation {

	public static final String INITIAL_VERTEX = "INITVERTEX";
	private final GraphDatabaseService graphDatabase;
	private final ForestFireModelParameters parameters;
	private final Random random;

	/**
	 * @param graphDatabase graph database representing the input graph
	 * @param parameters    parameters for the forest fire model
	 */
	public ForestFireModelComputation(GraphDatabaseService graphDatabase, ForestFireModelParameters parameters) {
		this.graphDatabase = graphDatabase;
		this.parameters = parameters;
		this.random = new Random();
	}

	/**
	 * Executes the forest fire model algorithm by creating new vertices and edges according to the model and the
	 * provided parameters. For each new vertex a random vertex is chosen to be the "ambassador", or initial vertex,
	 * to which an edge is formed. The id of this ambassador is stored as INITIAL_VERTEX property of the new vertex.
	 * Next, edges are formed to a random set of out- and in-neighbours for a specified number of iterations.
	 */
	public void run() {
		try (Transaction transaction = graphDatabase.beginTx()) {
			for (long newVertexId = parameters.getMaxId() + 1;
			     newVertexId <= parameters.getMaxId() + parameters.getNumNewVertices();
			     newVertexId++) {
				Node newVertex = createNewVertex(newVertexId);
				propagateFire(newVertex);
			}
			transaction.success();
		}
	}

	private void propagateFire(Node newVertex) {
	}

	private Node createNewVertex(long newVertexId) {
		// Select a random ambassador
		Node ambassador = null;
		float maxValue = Float.NEGATIVE_INFINITY;
		for (Node possibleAmbassador : GlobalGraphOperations.at(graphDatabase).getAllNodes()) {
			float randomValue = random.nextFloat();
			if (randomValue > maxValue) {
				ambassador = possibleAmbassador;
				maxValue = randomValue;
			}
		}

		// Create the new vertex and an edge to the ambassador
		Node newNode = graphDatabase.createNode();
		newNode.setProperty(Neo4jConfiguration.ID_PROPERTY, newVertexId);
		newNode.setProperty(INITIAL_VERTEX, ambassador.getProperty(Neo4jConfiguration.ID_PROPERTY));
		newNode.createRelationshipTo(ambassador, Neo4jConfiguration.EDGE);

		return newNode;
	}

}
