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
package science.atlarge.graphalytics.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * In-memory representation of the values of a graph, i.e. a value for each vertex.
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class GraphValues<ValueType> {

	private final Map<Long, ValueType> vertexValues;

	/**
	 * @param vertexValues a map containing a vertex value for each vertex
	 */
	public GraphValues(Map<Long, ValueType> vertexValues) {
		this.vertexValues = new HashMap<>(vertexValues);
	}

	/**
	 * @return a set of vertex ids in the graph
	 */
	public Set<Long> getVertices() {
		return vertexValues.keySet();
	}

	/**
	 * @param vertexId the id of a vertex in the graph
	 * @return the corresponding vertex value
	 */
	public ValueType getVertexValue(long vertexId) {
		return vertexValues.get(vertexId);
	}

}
