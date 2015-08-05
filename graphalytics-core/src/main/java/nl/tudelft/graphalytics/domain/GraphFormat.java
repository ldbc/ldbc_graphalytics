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
 * Wrapper for graph format information describing both the directivity of the
 * graph and whether it is stored using a vertex- or edge-based encoding.
 *
 * @author Tim Hegeman
 */
public final class GraphFormat implements Serializable {

	private final boolean directed;
	private final boolean edgeBased;

	/**
	 * @param directed  true iff the graph is directed
	 * @param edgeBased true iff the graph is stored edge-based
	 */
	public GraphFormat(boolean directed, boolean edgeBased) {
		this.directed = directed;
		this.edgeBased = edgeBased;
	}

	/**
	 * @return true iff the graph is directed
	 */
	public boolean isDirected() {
		return directed;
	}

	/**
	 * @return true iff the graph is stored in edge-based format
	 */
	public boolean isEdgeBased() {
		return edgeBased;
	}

	@Override
	public String toString() {
		return "(" + (directed ? "directed" : "undirected") + "," +
				(edgeBased ? "edge-based" : "vertex-based") + ")";
	}
}
