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

import science.atlarge.graphalytics.util.graph.PropertyGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * In-memory representation of the graph structure of a graph, i.e. a set of outgoing edges for each vertex.
 *
 * @author Mihai Capotă
 * @author Stijn Heldens
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public final class GraphStructure {

	private final Map<Long, Set<Long>> edgeLists;

	/**
	 * @param edgeLists a map containing a set of outgoing edges for each vertex
	 */
	public GraphStructure(Map<Long, Set<Long>> edgeLists) {
		this.edgeLists = new HashMap<>(edgeLists);
	}

	/**
	 * @return a set of vertex ids in the graph
	 */
	public final Set<Long> getVertices() {
		return edgeLists.keySet();
	}

	/**
	 * @param vertexId the id of a vertex in the graph
	 * @return the corresponding set of outgoing edges
	 */
	public final Set<Long> getEdgesForVertex(long vertexId) {
		return edgeLists.get(vertexId);
	}

	/**
	 * Converts a GraphStructure object to a PropertyGraph without any properties.
	 *
	 * @return a PropertyGraph representation of the same graph
	 */
	public PropertyGraph<Void, Void> toPropertyGraph() {
		PropertyGraph<Void, Void> graph = new PropertyGraph<>();
		// Copy vertices
		for (long vertexId : edgeLists.keySet()) {
			graph.createVertex(vertexId, null);
		}
		// Copy edges
		for (Map.Entry<Long, Set<Long>> vertex : edgeLists.entrySet()) {
			long sourceId = vertex.getKey();
			for (long destinationId : vertex.getValue()) {
				graph.createEdge(sourceId, destinationId, null);
			}
		}
		return graph;
	}

}
