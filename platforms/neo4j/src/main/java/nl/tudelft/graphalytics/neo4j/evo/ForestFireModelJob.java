package nl.tudelft.graphalytics.neo4j.evo;

import nl.tudelft.graphalytics.domain.algorithms.ForestFireModelParameters;
import nl.tudelft.graphalytics.neo4j.Neo4jJob;
import org.neo4j.graphdb.GraphDatabaseService;

import java.net.URL;

/**
 * Neo4j job configuration for executing the forest fire model for graph evolution.
 *
 * @author Tim Hegeman
 */
public class ForestFireModelJob extends Neo4jJob {

	private final ForestFireModelParameters parameters;

	/**
	 * @param databasePath   the path of the pre-loaded graph database
	 * @param propertiesFile a Neo4j properties file
	 * @param parameters     a ForestFireModelParameters object specifying the model-specific parameters
	 */
	public ForestFireModelJob(String databasePath, URL propertiesFile, Object parameters) {
		super(databasePath, propertiesFile);
		this.parameters = (ForestFireModelParameters) parameters;
	}

	@Override
	public void runComputation(GraphDatabaseService graphDatabase) {
		new ForestFireModelComputation(graphDatabase, parameters);
	}

}
