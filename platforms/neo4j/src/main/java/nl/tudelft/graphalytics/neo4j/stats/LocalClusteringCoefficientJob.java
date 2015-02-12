package nl.tudelft.graphalytics.neo4j.stats;

import nl.tudelft.graphalytics.neo4j.Neo4jJob;
import nl.tudelft.graphalytics.neo4j.stats.LocalClusteringCoefficientComputation.LocalClusteringCoefficientResult;
import org.neo4j.graphdb.GraphDatabaseService;

import java.net.URL;

/**
 * Neo4j job configuration for calculating the (mean) local clustering coefficient.
 *
 * @author Tim Hegeman
 */
public class LocalClusteringCoefficientJob extends Neo4jJob {

	private LocalClusteringCoefficientResult result;

	/**
	 * @param databasePath   the path of the pre-loaded graph database
	 * @param propertiesFile a Neo4j properties file
	 */
	public LocalClusteringCoefficientJob(String databasePath, URL propertiesFile) {
		super(databasePath, propertiesFile);
	}

	@Override
	public void runComputation(GraphDatabaseService graphDatabase) {
		result = new LocalClusteringCoefficientComputation(graphDatabase).run();
	}

	public LocalClusteringCoefficientResult getResult() {
		return result;
	}

}
