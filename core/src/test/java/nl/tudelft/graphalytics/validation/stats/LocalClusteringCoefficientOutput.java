package nl.tudelft.graphalytics.validation.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Tim Hegeman
 */
public class LocalClusteringCoefficientOutput {

	private final Map<Long, Double> localClusteringCoefficients;
	private final double meanLocalClusteringCoefficient;

	public LocalClusteringCoefficientOutput(Map<Long, Double> localClusteringCoefficients,
	                                        double meanLocalClusteringCoefficient) {
		this.localClusteringCoefficients = new HashMap<>(localClusteringCoefficients);
		this.meanLocalClusteringCoefficient = meanLocalClusteringCoefficient;
	}

	public Set<Long> getVertices() {
		return localClusteringCoefficients.keySet();
	}

	public double getComponentIdForVertex(long vertexId) {
		return localClusteringCoefficients.get(vertexId);
	}

	public double getMeanLocalClusteringCoefficient() {
		return meanLocalClusteringCoefficient;
	}

}
