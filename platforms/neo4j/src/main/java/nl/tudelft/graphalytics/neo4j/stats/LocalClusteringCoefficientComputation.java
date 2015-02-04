package nl.tudelft.graphalytics.neo4j.stats;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Implementation of the local clustering coefficient algorithm in Neo4j. This class is responsible for the computation,
 * given a functional Neo4j database instance.
 *
 * @author Tim Hegeman
 */
public class LocalClusteringCoefficientComputation {

	public static final String LCC = "LCC";
	private final GraphDatabaseService graphDatabase;

	/**
	 * @param graphDatabase graph database representing the input graph
	 */
	public LocalClusteringCoefficientComputation(GraphDatabaseService graphDatabase) {
		this.graphDatabase = graphDatabase;
	}

	/**
	 * Executes the local clustering coefficient algorithm by setting the LCC property on all nodes, and returning
	 * the mean value as a result.
	 */
	public LocalClusteringCoefficientResult run() {
		return new LocalClusteringCoefficientResult(Double.NaN);
	}

	/**
	 * Data container for the result of the local clustering coefficient algorithm.
	 */
	public static class LocalClusteringCoefficientResult {

		private final double meanLcc;

		/**
		 * @param meanLcc the mean local clustering coefficient of the graph
		 */
		public LocalClusteringCoefficientResult(double meanLcc) {
			this.meanLcc = meanLcc;
		}

		/**
		 * @return the mean local clustering coefficient of the graph
		 */
		public double getMeanLcc() {
			return meanLcc;
		}

	}

}
