package nl.tudelft.graphalytics.neo4j.evo;

import nl.tudelft.graphalytics.domain.algorithms.ForestFireModelParameters;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Implementation of the forest fire model for graph evolution using Neo4j.
 *
 * @author Tim Hegeman
 */
public class ForestFireModelComputation {

	public static final String INITIAL_VERTEX = "INITVERTEX";
	private final GraphDatabaseService graphDatabase;
	private final ForestFireModelParameters parameters;

	/**
	 * @param graphDatabase graph database representing the input graph
	 * @param parameters    parameters for the forest fire model
	 */
	public ForestFireModelComputation(GraphDatabaseService graphDatabase, ForestFireModelParameters parameters) {
		this.graphDatabase = graphDatabase;
		this.parameters = parameters;
	}

	/**
	 * Executes the forest fire model algorithm by creating new vertices and edges according to the model and the
	 * provided parameters. For each new vertex a random vertex is chosen to be the "ambassador", or initial vertex,
	 * to which an edge is formed. The id of this ambassador is stored as INITIAL_VERTEX property of the new vertex.
	 * Next, edges are formed to a random set of out- and in-neighbours for a specified number of iterations.
	 */
	public void run() {
		
	}

}
