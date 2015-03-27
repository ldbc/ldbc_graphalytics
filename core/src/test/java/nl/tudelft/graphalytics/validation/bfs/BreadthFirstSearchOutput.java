package nl.tudelft.graphalytics.validation.bfs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Tim Hegeman
 */
public class BreadthFirstSearchOutput {

	private final Map<Long, Long> pathLengths;

	public BreadthFirstSearchOutput(Map<Long, Long> pathLengths) {
		this.pathLengths = new HashMap<>(pathLengths);
	}

	public Set<Long> getVertices() {
		return pathLengths.keySet();
	}

	public long getPathLengthForVertex(long vertexId) {
		return pathLengths.get(vertexId);
	}

}
