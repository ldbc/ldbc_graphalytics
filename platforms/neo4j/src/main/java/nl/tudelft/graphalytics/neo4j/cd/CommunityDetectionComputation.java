package nl.tudelft.graphalytics.neo4j.cd;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Implementation of the community detection algorithm in Neo4j. This class is responsible for the computation,
 * given a functional Neo4j database instance.
 *
 * @author Tim Hegeman
 */
public class CommunityDetectionComputation {

	public static final String LABEL = "LABEL";
	private final GraphDatabaseService graphDatabase;
	private final float nodePreference;
	private final float hopAttenuation;
	private final int maxIterations;

	/**
	 * @param graphDatabase  graph database representing the input graph
	 * @param nodePreference node preference parameter to the label propagation algorithm
	 * @param hopAttenuation hop attenuation parameter to the label propagation algorithm
	 * @param maxIterations  maximum number of iterations of the label propagation to run
	 */
	public CommunityDetectionComputation(GraphDatabaseService graphDatabase, float nodePreference,
	                                     float hopAttenuation, int maxIterations) {
		this.graphDatabase = graphDatabase;
		this.nodePreference = nodePreference;
		this.hopAttenuation = hopAttenuation;
		this.maxIterations = maxIterations;
	}

	/**
	 * Executes the community detection algorithm by setting the LABEL property of all nodes to the label of the
	 * community to which the node belongs.
	 */
	public void run() {

	}

}
