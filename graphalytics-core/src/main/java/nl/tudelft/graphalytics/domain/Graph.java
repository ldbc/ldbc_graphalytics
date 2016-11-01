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

import nl.tudelft.graphalytics.domain.graph.Property;
import nl.tudelft.graphalytics.domain.graph.PropertyList;

import java.io.Serializable;

/**
 * Represents a single graph in the Graphalytics benchmark suite. Each graph has a unique name, two paths to files
 * containing the vertex and edge data of the graph, a specification of the vertex and edge properties, and some
 * metadata.
 *
 * @author Tim Hegeman
 */
public final class Graph implements Serializable {

	// General graph information
	private final String name;
	private final long numberOfVertices;
	private final long numberOfEdges;
	private final boolean isDirected;

	// EVLP-format graph with properties (if applicable)
	private final String vertexFilePath;
	private final String edgeFilePath;
	private final PropertyList vertexProperties;
	private final PropertyList edgeProperties;

	// Pointer to collection of graphs with same source
	private GraphSet graphSet;

	/**
	 * @param graphSetName     the unique name of the graph set this graph belongs to
	 * @param numberOfVertices the number of vertices in the graph
	 * @param numberOfEdges    the number of edges in the graph
	 * @param isDirected       true iff the graph is directed
	 * @param vertexFilePath   the path of the vertex data file
	 * @param edgeFilePath     the path of the edge data file
	 * @param vertexProperties an ordered list of names and types of the properties of each vertex
	 * @param edgeProperties   an ordered list of names and types of the properties of each edge
	 */
	public Graph(String graphSetName, long numberOfVertices, long numberOfEdges, boolean isDirected,
			String vertexFilePath, String edgeFilePath, PropertyList vertexProperties, PropertyList edgeProperties) {
		this.numberOfVertices = numberOfVertices;
		this.numberOfEdges = numberOfEdges;
		this.isDirected = isDirected;
		this.vertexFilePath = vertexFilePath;
		this.edgeFilePath = edgeFilePath;
		this.vertexProperties = vertexProperties;
		this.edgeProperties = edgeProperties;
		this.name = generateUniqueName(graphSetName, vertexProperties, edgeProperties);
	}

	private static String generateUniqueName(String graphSetName, PropertyList vertexProperties,
			PropertyList edgeProperties) {
		StringBuilder nameBuilder = new StringBuilder(graphSetName);
		if (vertexProperties.size() > 0) {
			nameBuilder.append(".v");
			for (Property property : vertexProperties) {
				nameBuilder.append('_');
				nameBuilder.append(property.getName());
			}
		}
		if (edgeProperties.size() > 0) {
			nameBuilder.append(".e");
			for (Property property : edgeProperties) {
				nameBuilder.append('_');
				nameBuilder.append(property.getName());
			}
		}
		return nameBuilder.toString();
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
		return vertexProperties.size() != 0;
	}

	/**
	 * @return true iff the edges of the graph have properties
	 */
	public boolean hasEdgeProperties() {
		return edgeProperties.size() != 0;
	}

	/**
	 * @return an ordered list of names and types of the properties of each vertex
	 */
	public PropertyList getVertexProperties() {
		return vertexProperties;
	}

	/**
	 * @return an ordered list of names and types of the properties of each edge
	 */
	public PropertyList getEdgeProperties() {
		return edgeProperties;
	}

	/**
	 * @return set of graphs with the same data source as this graph
	 */
	public GraphSet getGraphSet() {
		return graphSet;
	}

	/**
	 * @param graphSet set of graphs with the same data source as this graph
	 */
	public void setGraphSet(GraphSet graphSet) {
		this.graphSet = graphSet;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Graph graph = (Graph)o;

		if (!name.equals(graph.name)) return false;
		if (!vertexProperties.equals(graph.vertexProperties)) return false;
		return edgeProperties.equals(graph.edgeProperties);

	}

//	@Override
//	public int hashCode() {
//		int result = name.hashCode();
//		result = 31 * result + vertexProperties.hashCode();
//		result = 31 * result + edgeProperties.hashCode();
//		return result;
//	}

}
