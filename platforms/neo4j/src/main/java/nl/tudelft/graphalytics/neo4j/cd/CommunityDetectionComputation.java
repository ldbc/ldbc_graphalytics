package nl.tudelft.graphalytics.neo4j.cd;

import nl.tudelft.graphalytics.neo4j.Neo4jConfiguration;
import org.neo4j.graphdb.*;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the community detection algorithm in Neo4j. This class is responsible for the computation,
 * given a functional Neo4j database instance.
 *
 * @author Tim Hegeman
 */
public class CommunityDetectionComputation {

	public static final String LABEL = "LABEL";
	public static final String LABEL_SCORE = "LABEL_SCORE";
	public static final String NEW_LABEL = "NEW_LABEL";
	public static final String NEW_LABEL_SCORE = "NEW_LABEL_SCORE";

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
		// Initialize the label of each node to its own ID
		initializeLabels();

		int iteration = 0;
		boolean converged = false;
		while (!converged && iteration < maxIterations) {
			converged = true;
			try (Transaction transaction = graphDatabase.beginTx()) {
				for (Node node : GlobalGraphOperations.at(graphDatabase).getAllNodes()) {
					computeNewLabel(node);
				}
				for (Node node : GlobalGraphOperations.at(graphDatabase).getAllNodes()) {
					converged = converged & updateLabel(node);
				}
				transaction.success();
			}
			iteration++;
		}
	}

	private void initializeLabels() {
		try (Transaction transaction = graphDatabase.beginTx()) {
			for (Node node : GlobalGraphOperations.at(graphDatabase).getAllNodes()) {
				node.setProperty(LABEL, node.getProperty(Neo4jConfiguration.ID_PROPERTY));
				node.setProperty(LABEL_SCORE, 1.0);
			}
			transaction.success();
		}
	}

	private void computeNewLabel(Node node) {
		Map<Long, Double> labelScores = new HashMap<>();
		Map<Long, Double> maxLabelScores = new HashMap<>();
		for (Relationship edge : node.getRelationships(Neo4jConfiguration.EDGE, Direction.BOTH)) {
			Node other = edge.getOtherNode(node);
			long label = (long) other.getProperty(LABEL);
			int degree = other.getDegree(Neo4jConfiguration.EDGE, Direction.BOTH);
			double score = (double) other.getProperty(LABEL_SCORE);
			double weighedScore = score * Math.pow(degree, nodePreference);

			if (!labelScores.containsKey(label)) {
				labelScores.put(label, weighedScore);
				maxLabelScores.put(label, score);
			} else {
				labelScores.put(label, weighedScore + labelScores.get(label));
				if (score > maxLabelScores.get(label))
					maxLabelScores.put(label, score);
			}
		}

		long maxLabel = 0;
		double maxLabelScore = Double.NEGATIVE_INFINITY;
		for (long label : labelScores.keySet()) {
			if (labelScores.get(label) > maxLabelScore) {
				maxLabel = label;
				maxLabelScore = labelScores.get(label);
			} else if (labelScores.get(label) == maxLabelScore && label < maxLabel) {
				maxLabel = label;
			}
		}

		maxLabelScore = maxLabelScores.get(maxLabel);
		if (maxLabel != (long) node.getProperty(LABEL)) {
			maxLabelScore -= hopAttenuation;
		}

		node.setProperty(NEW_LABEL, maxLabel);
		node.setProperty(NEW_LABEL_SCORE, maxLabelScore);
	}

	private boolean updateLabel(Node node) {
		if ((long) node.getProperty(LABEL) == (long) node.getProperty(NEW_LABEL)) {
			node.removeProperty(NEW_LABEL);
			node.removeProperty(NEW_LABEL_SCORE);
			return true;
		} else {
			node.setProperty(LABEL, node.getProperty(NEW_LABEL));
			node.setProperty(LABEL_SCORE, node.getProperty(NEW_LABEL_SCORE));
			node.removeProperty(NEW_LABEL);
			node.removeProperty(NEW_LABEL_SCORE);
			return false;
		}
	}

}
