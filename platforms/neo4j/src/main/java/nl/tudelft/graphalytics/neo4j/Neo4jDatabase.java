package nl.tudelft.graphalytics.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.net.URL;

/**
 * Wrapper class for the initialization and safe shutdown of a Neo4j database.
 *
 * @author Tim Hegeman
 */
public class Neo4jDatabase implements AutoCloseable {

	private final GraphDatabaseService graphDatabase;

	/**
	 * Initializes an embedded Neo4j database using data stored in the specified path, and using configuration specified
	 * in the provided properties file.
	 *
	 * @param databasePath   the path of the pre-loaded graph database
	 * @param propertiesFile a Neo4j properties file
	 */
	public Neo4jDatabase(String databasePath, URL propertiesFile) {
		this.graphDatabase = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(databasePath)
				.loadPropertiesFromURL(propertiesFile)
				.newGraphDatabase();
	}

	/**
	 * @return a handle to the Neo4j database
	 */
	public GraphDatabaseService get() {
		return graphDatabase;
	}

	@Override
	public void close() {
		graphDatabase.shutdown();
	}

}
