package nl.tudelft.graphalytics.neo4j.conn;

import nl.tudelft.graphalytics.neo4j.Neo4jJob;
import org.neo4j.graphdb.GraphDatabaseService;

import java.net.URL;

/**
 * Neo4j job configuration for executing the connected components algorithm.
 *
 * @author Tim Hegeman
 */
public class ConnectedComponentsJob extends Neo4jJob {

	/**
	 * @param databasePath   the path of the pre-loaded graph database
	 * @param propertiesFile a Neo4j properties file
	 */
	public ConnectedComponentsJob(String databasePath, URL propertiesFile) {
		super(databasePath, propertiesFile);
	}

	@Override
	public void runComputation(GraphDatabaseService graphDatabase) {
		new ConnectedComponentsComputation(graphDatabase).run();
	}

}
