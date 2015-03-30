package nl.tudelft.graphalytics.validation.bfs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Container for the output of the breadth-first search algorithm, used by the corresponding Graphalytics validation
 * test.
 *
 * @author Tim Hegeman
 */
public class BreadthFirstSearchOutput {

	private final Map<Long, Long> pathLengths;

	/**
	 * @param pathLengths a map containing the shortest path length to each vertex
	 */
	public BreadthFirstSearchOutput(Map<Long, Long> pathLengths) {
		this.pathLengths = new HashMap<>(pathLengths);
	}

	/**
	 * @return a set of vertex ids for which the shortest path length is known
	 */
	public Set<Long> getVertices() {
		return pathLengths.keySet();
	}

	/**
	 * @param vertexId the id of a vertex in the output data
	 * @return the corresponding shortest path length
	 */
	public long getPathLengthForVertex(long vertexId) {
		return pathLengths.get(vertexId);
	}

}
