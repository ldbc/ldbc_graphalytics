package nl.tudelft.graphalytics.validation.algorithms.sssp;

import java.util.Map;
import java.util.Set;

/**
 * Container for the output of the SSSP algorithm, used by the corresponding Graphalytics validation test.
 *
 * @author Tim Hegeman
 */
public class SingleSourceShortestPathsOutput {

	private final Map<Long, Double> distances;

	/**
	 * @param distances a map containing the distance from the source for each vertex
	 */
	public SingleSourceShortestPathsOutput(Map<Long, Double> distances) {
		this.distances = distances;
	}

	/**
	 * @return a set of vertex ids for which the distance is known
	 */
	public Set<Long> getVertices() {
		return distances.keySet();
	}

	/**
	 * @param vertexId the id of a vertex in the output data
	 * @return the corresponding distance
	 */
	public double getDistanceForVertex(long vertexId) {
		return distances.get(vertexId);
	}

}
