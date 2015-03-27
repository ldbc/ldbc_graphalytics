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

	public GraphStructure(Map<Long, Set<Long>> edgeLists) {
		this.edgeLists = new HashMap<>(edgeLists);
	}

	public final Set<Long> getEdgesForVertex(long vertexId) {
		return edgeLists.get(vertexId);
	}

	public final Set<Long> getVertices() {
		return edgeLists.keySet();
	}

}
