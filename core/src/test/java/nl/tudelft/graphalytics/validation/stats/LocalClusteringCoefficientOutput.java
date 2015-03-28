package nl.tudelft.graphalytics.validation.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Container for output of the local clustering coefficient algorithm, used by Graphalytics validation test.
 *
 * @author Tim Hegeman
 */
public class LocalClusteringCoefficientOutput {

	private final Map<Long, Double> localClusteringCoefficients;
	private final double meanLocalClusteringCoefficient;

	/**
	 * @param localClusteringCoefficients    a map containing the local clustering coefficient of each vertex
	 * @param meanLocalClusteringCoefficient the mean local clustering coefficient
	 */
	public LocalClusteringCoefficientOutput(Map<Long, Double> localClusteringCoefficients,
	                                        double meanLocalClusteringCoefficient) {
		this.localClusteringCoefficients = new HashMap<>(localClusteringCoefficients);
		this.meanLocalClusteringCoefficient = meanLocalClusteringCoefficient;
	}

	/**
	 * @return a set of vertex ids for which the component id is known
	 */
	public Set<Long> getVertices() {
		return localClusteringCoefficients.keySet();
	}

	/**
	 * @param vertexId the id of a vertex in the output data
	 * @return the corresponding local clustering coefficient
	 */
	public double getLocalClusteringCoefficientForVertex(long vertexId) {
		return localClusteringCoefficients.get(vertexId);
	}

	/**
	 * @return the mean local clustering coefficient
	 */
	public double getMeanLocalClusteringCoefficient() {
		return meanLocalClusteringCoefficient;
	}

}
