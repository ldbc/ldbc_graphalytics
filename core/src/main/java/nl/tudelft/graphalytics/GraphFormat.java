package nl.tudelft.graphalytics;

import java.io.Serializable;

/**
 * Wrapper for graph format information describing both the directivity of the
 * graph and whether it is stored using a vertex- or edge-based encoding.
 * 
 * @author Tim Hegeman
 */
public class GraphFormat implements Serializable {

	private static final long serialVersionUID = -2453101476739445484L;
	
	private boolean directed;
	private boolean edgeBased;
	
	/**
	 * @param directed true iff the graph is directed
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
	 * @return true iff the graph is undirected
	 */
	public boolean isUndirected() {
		return !isDirected();
	}

	/**
	 * @return true iff the graph is stored in edge-based format
	 */
	public boolean isEdgeBased() {
		return edgeBased;
	}
	
	/**
	 * @return true iff the graph is stored in vertex-based format
	 */
	public boolean isVertexBased() {
		return !isEdgeBased();
	}
	
	@Override
	public String toString() {
		return "(" + (directed ? "directed" : "undirected") + "," +
				(edgeBased ? "edge-based" : "vertex-based") + ")";
	}
	
}
