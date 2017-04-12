/*
 * Copyright 2015 Delft University of Technology
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
package science.atlarge.graphalytics.validation.algorithms.wcc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Container for the output of the weakly connected components algorithm, used by the corresponding Graphalytics
 * validation test.
 *
 * @author Tim Hegeman
 */
public class WeaklyConnectedComponentsOutput {

	private final Map<Long, Long> componentIds;

	/**
	 * @param componentIds a map containing the component id of each vertex
	 */
	public WeaklyConnectedComponentsOutput(Map<Long, Long> componentIds) {
		this.componentIds = new HashMap<>(componentIds);
	}

	/**
	 * @return a set of vertex ids for which the component id is known
	 */
	public Set<Long> getVertices() {
		return componentIds.keySet();
	}

	/**
	 * @param vertexId the id of a vertex in the output data
	 * @return the corresponding component id
	 */
	public long getComponentIdForVertex(long vertexId) {
		return componentIds.get(vertexId);
	}

}
