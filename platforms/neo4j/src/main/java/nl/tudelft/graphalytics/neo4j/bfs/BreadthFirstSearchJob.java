package nl.tudelft.graphalytics.neo4j.bfs;

import nl.tudelft.graphalytics.domain.algorithms.BreadthFirstSearchParameters;
import nl.tudelft.graphalytics.neo4j.Neo4jJob;
import org.neo4j.graphdb.GraphDatabaseService;

import java.net.URL;

/**
 * Neo4j job configuration for executing the breadth-first search algorithm.
 *
 * @author Tim Hegeman
 */
public class BreadthFirstSearchJob extends Neo4jJob {

	private final BreadthFirstSearchParameters parameters;

	/**
	 * @param databasePath   path to the Neo4j database representing the graph
	 * @param propertiesFile URL of a neo4j.properties file to load from
	 * @param parameters     algorithm-specific parameters, must be of type BreadthFirstSearchParameters
	 */
	public BreadthFirstSearchJob(String databasePath, URL propertiesFile, Object parameters) {
		super(databasePath, propertiesFile);
		this.parameters = (BreadthFirstSearchParameters) parameters;
	}

	@Override
	public void runComputation(GraphDatabaseService graphDatabase) {
		new BreadthFirstSearchComputation(graphDatabase, parameters.getSourceVertex()).run();
	}

}
