package nl.tudelft.graphalytics.validation.cd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Tim Hegeman
 */
public class CommunityDetectionOutput {

	private final Map<Long, Long> communityIds;

	public CommunityDetectionOutput(Map<Long, Long> communityIds) {
		this.communityIds = new HashMap<>(communityIds);
	}

	public Set<Long> getVertices() {
		return communityIds.keySet();
	}

	public Set<Long> getCommunities() {
		return new HashSet<>(communityIds.values());
	}

	public long getCommunityIdForVertex(long vertexId) {
		return communityIds.get(vertexId);
	}

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
