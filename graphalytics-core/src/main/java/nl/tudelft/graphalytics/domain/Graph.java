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
package nl.tudelft.graphalytics.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single graph in the Graphalytics benchmark suite. Each graph has a unique name, two paths to files
 * containing the vertex and edge data of the graph, a format specification, and some metadata.
 *
 * @author Tim Hegeman
 */
public final class Graph implements Serializable {

	// General graph information
	private final String name;
	private final long numberOfVertices;
	private final long numberOfEdges;
	private final boolean isDirected;

	// EVL-format graph structure
	private final String vertexFilePath;
	private final String edgeFilePath;

	// EVLP-format graph with properties (if applicable)
	private final String vertexPropertyFilePath;
	private final String edgePropertyFilePath;
	private final List<String> vertexPropertyNames;
	private final List<PropertyType> vertexPropertyTypes;
	private final List<String> edgePropertyNames;
	private final List<PropertyType> edgePropertyTypes;

	/**
	 * @param name                   the unique name of the graph
	 * @param numberOfVertices       the number of vertices in the graph
	 * @param numberOfEdges          the number of edges in the graph
	 * @param isDirected             true iff the graph is directed
	 * @param vertexFilePath         the path of the vertex data file
	 * @param edgeFilePath           the path of the edge data file
	 * @param vertexPropertyFilePath the path of the vertex data file with properties
	 * @param edgePropertyFilePath   the path of the edge data file with properties
	 * @param vertexPropertyNames    an ordered list of names of the properties of each vertex
	 * @param vertexPropertyTypes    an ordered list of types of the properties of each vertex
	 * @param edgePropertyNames      an ordered list of names of the properties of each edge
	 * @param edgePropertyTypes      an ordered list of types of the properties of each edge
	 */
	private Graph(String name, long numberOfVertices, long numberOfEdges, boolean isDirected, String vertexFilePath,
			String edgeFilePath, String vertexPropertyFilePath, String edgePropertyFilePath,
			List<String> vertexPropertyNames, List<PropertyType> vertexPropertyTypes,
			List<String> edgePropertyNames, List<PropertyType> edgePropertyTypes) {
		this.name = name;
		this.numberOfVertices = numberOfVertices;
		this.numberOfEdges = numberOfEdges;
		this.isDirected = isDirected;
		this.vertexFilePath = vertexFilePath;
		this.edgeFilePath = edgeFilePath;
		this.vertexPropertyFilePath = vertexPropertyFilePath;
		this.edgePropertyFilePath = edgePropertyFilePath;
		this.vertexPropertyNames = Collections.unmodifiableList(new ArrayList<>(vertexPropertyNames));
		this.vertexPropertyTypes = Collections.unmodifiableList(new ArrayList<>(vertexPropertyTypes));
		this.edgePropertyNames = Collections.unmodifiableList(new ArrayList<>(edgePropertyNames));
		this.edgePropertyTypes = Collections.unmodifiableList(new ArrayList<>(edgePropertyTypes));
	}

	/**
	 * @return the unique name of the graph
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the number of vertices in the graph
	 */
	public long getNumberOfVertices() {
		return numberOfVertices;
	}

	/**
	 * @return the number of edges in the graph
	 */
	public long getNumberOfEdges() {
		return numberOfEdges;
	}

	/**
	 * @return true iff the graph is directed
	 */
	public boolean isDirected() {
		return isDirected;
	}

	/**
	 * @return the graph format specification
	 */
	@Deprecated
	public GraphFormat getGraphFormat() {
		return new GraphFormat(isDirected);
	}

	/**
	 * @return the path of the vertex data file
	 */
	public String getVertexFilePath() {
		return vertexFilePath;
	}

	/**
	 * @return the path of the edge data file
	 */
	public String getEdgeFilePath() {
		return edgeFilePath;
	}

	/**
	 * @return true iff the vertices of the graph have properties
	 */
	public boolean hasVertexProperties() {
		return !vertexPropertyNames.isEmpty();
	}

	/**
	 * @return true iff the edges of the graph have properties
	 */
	public boolean hasEdgeProperties() {
		return !edgePropertyNames.isEmpty();
	}

	/**
	 * @return the path of the vertex data file with properties
	 */
	public String getVertexPropertyFilePath() {
		return vertexPropertyFilePath;
	}

	/**
	 * @return the path of the edge data file with properties
	 */
	public String getEdgePropertyFilePath() {
		return edgePropertyFilePath;
	}

	/**
	 * @return an ordered list of names of the properties of each vertex
	 */
	public List<String> getVertexPropertyNames() {
		return vertexPropertyNames;
	}

	/**
	 * @return an ordered list of types of the properties of each vertex
	 */
	public List<PropertyType> getVertexPropertyTypes() {
		return vertexPropertyTypes;
	}

	/**
	 * @return an ordered list of names of the properties of each edge
	 */
	public List<String> getEdgePropertyNames() {
		return edgePropertyNames;
	}

	/**
	 * @return an ordered list of types of the properties of each edge
	 */
	public List<PropertyType> getEdgePropertyTypes() {
		return edgePropertyTypes;
	}

	@Override
	public String toString() {
		return "Graph{" +
				"name='" + name + '\'' +
				'}';
	}

	/**
	 * Helper class for constructing a Graph instance.
	 */
	public static class Builder {

		// General graph information
		private final String name;
		private final long numberOfVertices;
		private final long numberOfEdges;
		private final boolean isDirected;

		// EVL-format graph structure
		private final String vertexFilePath;
		private final String edgeFilePath;

		// EVLP-format graph with properties (if applicable)
		private String vertexPropertyFilePath;
		private String edgePropertyFilePath;
		private List<String> vertexPropertyNames;
		private List<PropertyType> vertexPropertyTypes;
		private List<String> edgePropertyNames;
		private List<PropertyType> edgePropertyTypes;

		public Builder(String name, long numberOfVertices, long numberOfEdges, boolean isDirected, String vertexFilePath, String edgeFilePath) {
			this.name = name;
			this.numberOfVertices = numberOfVertices;
			this.numberOfEdges = numberOfEdges;
			this.isDirected = isDirected;
			this.vertexFilePath = vertexFilePath;
			this.edgeFilePath = edgeFilePath;
			this.vertexPropertyFilePath = vertexFilePath;
			this.edgePropertyFilePath = edgeFilePath;
			this.vertexPropertyNames = Collections.emptyList();
			this.vertexPropertyTypes = Collections.emptyList();
			this.edgePropertyNames = Collections.emptyList();
			this.edgePropertyTypes = Collections.emptyList();
		}

		public Builder withVertexProperties(String vertexPropertyFilePath, List<String> vertexPropertyNames, List<PropertyType> vertexPropertyTypes) {
			this.vertexPropertyFilePath = vertexPropertyFilePath;
			this.vertexPropertyNames = vertexPropertyNames;
			this.vertexPropertyTypes = vertexPropertyTypes;
			return this;
		}

		public Builder withEdgeProperties(String edgePropertyFilePath, List<String> edgePropertyNames, List<PropertyType> edgePropertyTypes) {
			this.edgePropertyFilePath = edgePropertyFilePath;
			this.edgePropertyNames = edgePropertyNames;
			this.edgePropertyTypes = edgePropertyTypes;
			return this;
		}

		public Graph toGraph() {
			return new Graph(name, numberOfVertices, numberOfEdges, isDirected, vertexFilePath, edgeFilePath,
					vertexPropertyFilePath, edgePropertyFilePath, vertexPropertyNames, vertexPropertyTypes,
					edgePropertyNames, edgePropertyTypes);
		}

	}

}
