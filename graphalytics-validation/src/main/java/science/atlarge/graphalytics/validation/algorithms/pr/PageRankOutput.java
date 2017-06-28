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
package science.atlarge.graphalytics.validation.algorithms.pr;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Container for the output of the PageRank algorithm, used by the corresponding Graphalytics validation test.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
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
