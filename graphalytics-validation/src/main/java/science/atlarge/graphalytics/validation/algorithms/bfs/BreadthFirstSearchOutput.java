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
package science.atlarge.graphalytics.validation.algorithms.bfs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Container for the output of the breadth-first search algorithm, used by the corresponding Graphalytics validation
 * test.
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class BreadthFirstSearchOutput {

	private final Map<Long, Long> pathLengths;

	/**
	 * @param pathLengths a map containing the shortest path length to each vertex
	 */
	public BreadthFirstSearchOutput(Map<Long, Long> pathLengths) {
		this.pathLengths = new HashMap<>(pathLengths);
	}

	/**
	 * @return a set of vertex ids for which the shortest path length is known
	 */
	public Set<Long> getVertices() {
		return pathLengths.keySet();
	}

	/**
	 * @param vertexId the id of a vertex in the output data
	 * @return the corresponding shortest path length
	 */
	public long getPathLengthForVertex(long vertexId) {
		return pathLengths.get(vertexId);
	}

}
