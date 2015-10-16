package nl.tudelft.graphalytics.validation.pr;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Container for the output of the PageRank algorithm, used by the corresponding Graphalytics validation test.
 *
 * @author Tim Hegeman
 */
public class PageRankOutput {

	private final Map<Long, Double> nodeRanks;

	/**
 	 * @param nodeRanks a map containing the rank of each vertex in the test graph
	 */
	public PageRankOutput(Map<Long, Double> nodeRanks) {
		this.nodeRanks = new HashMap<>(nodeRanks);
	}

	/**
	 * @return a set of vertex ids for which the rank is known
	 */
	public Set<Long> getVertices() {
		return nodeRanks.keySet();
	}

	/**
	 * @param vertexId the id of a vertex in the output data
	 * @return the corresponding rank
	 */
	public double getRankForVertex(long vertexId) {
		return nodeRanks.get(vertexId);
	}

}
