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
package science.atlarge.graphalytics.util.graph;

import java.util.*;

/**
 * In-memory representation of a property graph, i.e. a set of vertices with corresponding values, and for each vertex
 * a set of outgoing edges with corresponding values.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class PropertyGraph<V, E> {

	private final Map<Long, Vertex> vertices;

	public PropertyGraph() {
		this.vertices = new HashMap<>();
	}

	public void createVertex(long id, V value) {
		if (vertices.containsKey(id)) {
			throw new IllegalArgumentException("Vertex with id " + id + " already exists.");
		}
		vertices.put(id, new Vertex(id, value));
	}

	public void createEdge(long sourceVertexId, long destinationVertexId, E value) {
		if (!vertices.containsKey(sourceVertexId)) {
			throw new IllegalArgumentException("Source vertex with id " + sourceVertexId + " does not exist.");
		}
		if (!vertices.containsKey(destinationVertexId)) {
			throw new IllegalArgumentException("Source vertex with id " + destinationVertexId + " does not exist.");
		}
		new Edge(vertices.get(sourceVertexId), vertices.get(destinationVertexId), value);
	}

	public Collection<Vertex> getVertices() {
		return Collections.unmodifiableCollection(vertices.values());
	}

	public Vertex getVertex(long id) {
		return vertices.get(id);
	}

	public class Vertex {

		final long id;
		final V value;
		final Collection<Edge> outgoingEdges;
		final Collection<Edge> incomingEdges;

		private Vertex(long id, V value) {
			this.id = id;
			this.value = value;
			this.outgoingEdges = new ArrayList<>();
			this.incomingEdges = new ArrayList<>();
		}

		public long getId() {
			return id;
		}

		public V getValue() {
			return value;
		}

		public Collection<Edge> getOutgoingEdges() {
			return Collections.unmodifiableCollection(outgoingEdges);
		}

		public Collection<Edge> getIncomingEdges() {
			return Collections.unmodifiableCollection(incomingEdges);
		}

		private void addEdge(Edge edge) {
			if (edge.getSourceVertex() == this) {
				outgoingEdges.add(edge);
			} else {
				incomingEdges.add(edge);
			}
		}

	}

	public class Edge {

		final Vertex sourceVertex;
		final Vertex destinationVertex;
		final E value;

		private Edge(Vertex sourceVertex, Vertex destinationVertex, E value) {
			this.sourceVertex = sourceVertex;
			this.destinationVertex = destinationVertex;
			this.value = value;

			sourceVertex.addEdge(this);
			destinationVertex.addEdge(this);
		}

		public Vertex getSourceVertex() {
			return sourceVertex;
		}

		public Vertex getDestinationVertex() {
			return destinationVertex;
		}

		public E getValue() {
			return value;
		}

		public Vertex getOtherEndpoint(Vertex vertex) {
			return sourceVertex == vertex ? destinationVertex : sourceVertex;
		}

	}

}
