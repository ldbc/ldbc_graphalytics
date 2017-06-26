/*
 * Copyright 2015 - 2017 Atlarge Research Team,
 * operating at Technische Universiteit Delft
 * and Vrije Universiteit Amsterdam, the Netherlands.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package science.atlarge.graphalytics.validation.algorithms.cdlp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Container for the output of the community detection algorithm, used by the corresponding Graphalytics validation
 * test.
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class CommunityDetectionLPOutput {

	private final Map<Long, Long> communityIds;

	/**
	 * @param communityIds a map containing the community id of each vertex
	 */
	public CommunityDetectionLPOutput(Map<Long, Long> communityIds) {
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
