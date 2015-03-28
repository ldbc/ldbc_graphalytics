package nl.tudelft.graphalytics.validation.cd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Container for output of the community detection algorithm, used by Graphalytics validation test.
 *
 * @author Tim Hegeman
 */
public class CommunityDetectionOutput {

	private final Map<Long, Long> communityIds;

	/**
	 * @param communityIds a map containing the community id of each vertex
	 */
	public CommunityDetectionOutput(Map<Long, Long> communityIds) {
		this.communityIds = new HashMap<>(communityIds);
	}

	/**
	 * @return a set of vertex ids for which the community id is known
	 */
	public Set<Long> getVertices() {
		return communityIds.keySet();
	}

	/**
	 * @return a set of community ids that belong to at least one vertex
	 */
	public Set<Long> getCommunities() {
		return new HashSet<>(communityIds.values());
	}

	/**
	 * @param vertexId the id of a vertex in the output data
	 * @return the corresponding community id
	 */
	public long getCommunityIdForVertex(long vertexId) {
		return communityIds.get(vertexId);
	}

	/**
	 * @param communityId the id of a community in the output data
	 * @return the set of vertices that belong to the given community
	 */
	public Set<Long> getVerticesInCommunity(long communityId) {
		Set<Long> verticesInCommunity = new HashSet<>();
		for (Map.Entry<Long, Long> vertex : communityIds.entrySet()) {
			if (vertex.getValue() == communityId) {
				verticesInCommunity.add(vertex.getKey());
			}
		}
		return verticesInCommunity;
	}

}
