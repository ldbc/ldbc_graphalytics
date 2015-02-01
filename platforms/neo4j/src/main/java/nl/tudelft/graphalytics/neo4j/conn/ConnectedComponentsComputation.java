package nl.tudelft.graphalytics.neo4j.conn;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Implementation of the connected components algorithm in Neo4j. This class is responsible for the computation,
 * given a functional Neo4j database instance.
 *
 * @author Tim Hegeman
 */
public class ConnectedComponentsComputation {

	public static final String COMPONENT = "COMPONENT";
	private final GraphDatabaseService graphDatabase;

	/**
	 * @param graphDatabase graph database representing the input graph
	 */
	public ConnectedComponentsComputation(GraphDatabaseService graphDatabase) {
		this.graphDatabase = graphDatabase;
	}

	/**
	 * Executes the connected components algorithm by setting the COMPONENT property of all nodes to the smallest node
	 * ID in each component.
	 */
	public void run() {

	}

}
