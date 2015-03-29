package nl.tudelft.graphalytics.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * In-memory representation of the graph structure of a graph, i.e. a set of outgoing edges for each vertex.
 *
 * @author Tim Hegeman
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

}
