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

/**
 * Represents a single graph in the Graphalytics benchmark suite. Each graph has a unique name, two paths to files
 * containing the vertex and edge data of the graph, a format specification, and some metadata.
 *
 * @author Tim Hegeman
 */
public final class Graph implements Serializable {

	private final String name;
	private final String vertexFilePath;
	private final String edgeFilePath;
	private final GraphFormat graphFormat;
	private final long numberOfVertices;
	private final long numberOfEdges;

	/**
	 * @param name             the unique name of the graph
	 * @param vertexFilePath   the path of the vertex data file, relative to the graph root directory
	 * @param edgeFilePath     the path of the edge data file, relative to the graph root directory
	 * @param graphFormat      the format of the graph
	 * @param numberOfVertices the number of vertices in the graph
	 * @param numberOfEdges    the number of edges in the graph
	 */
	public Graph(String name, String vertexFilePath, String edgeFilePath, GraphFormat graphFormat,
			long numberOfVertices, long numberOfEdges) {
		this.name = name;
		this.vertexFilePath = vertexFilePath;
		this.edgeFilePath = edgeFilePath;
		this.graphFormat = graphFormat;
		this.numberOfVertices = numberOfVertices;
		this.numberOfEdges = numberOfEdges;
	}

	/**
	 * @return the unique name of the graph
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the path of the vertex data file, relative to the graph root directory
	 */
	public String getVertexFilePath() {
		return vertexFilePath;
	}

	/**
	 * @return the path of the edge data file, relative to the graph root directory
	 */
	public String getEdgeFilePath() {
		return edgeFilePath;
	}

	/**
	 * @return the graph format specification
	 */
	public GraphFormat getGraphFormat() {
		return graphFormat;
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

	@Override
	public String toString() {
		return "Graph(name=\"" + name + "\",vpath=\"" + vertexFilePath + "\",epath=\"" + edgeFilePath +
				"\",format=" + graphFormat + ",#V=" + numberOfVertices + ",#E=" + numberOfEdges + ")";
	}

}
