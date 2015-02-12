package nl.tudelft.graphalytics.neo4j.cd;

import nl.tudelft.graphalytics.domain.algorithms.CommunityDetectionParameters;
import nl.tudelft.graphalytics.neo4j.Neo4jJob;
import org.neo4j.graphdb.GraphDatabaseService;

import java.net.URL;

/**
 * Neo4j job configuration for executing the community detection algorithm.
 *
 * @author Tim Hegeman
 */
public class CommunityDetectionJob extends Neo4jJob {

	private final CommunityDetectionParameters parameters;

	/**
	 * @param databasePath   path to the Neo4j database representing the graph
	 * @param propertiesFile URL of a neo4j.properties file to load from
	 * @param parameters     algorithm-specific parameters, must be of type BreadthFirstSearchParameters
	 */
	public CommunityDetectionJob(String databasePath, URL propertiesFile, Object parameters) {
		super(databasePath, propertiesFile);
		this.parameters = (CommunityDetectionParameters) parameters;
	}

	@Override
	public void runComputation(GraphDatabaseService graphDatabase) {
		new CommunityDetectionComputation(
				graphDatabase,
				parameters.getNodePreference(),
				parameters.getHopAttenuation(),
				parameters.getMaxIterations()
		).run();
	}

}
