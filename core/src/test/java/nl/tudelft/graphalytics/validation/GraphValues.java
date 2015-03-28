package nl.tudelft.graphalytics.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * In-memory representation of the values of a graph, i.e. a value for each vertex.
 *
 * @author Tim Hegeman
 */
public class GraphValues<ValueType> {

	private final Map<Long, ValueType> vertexValues;

	/**
	 * @param vertexValues a map containing a vertex value for each vertex
	 */
	public GraphValues(Map<Long, ValueType> vertexValues) {
		this.vertexValues = new HashMap<>(vertexValues);
	}

	/**
	 * @return a set of vertex ids in the graph
	 */
	public Set<Long> getVertices() {
		return vertexValues.keySet();
	}

	/**
	 * @param vertexId the id of a vertex in the graph
	 * @return the corresponding vertex value
	 */
	public ValueType getVertexValue(long vertexId) {
		return vertexValues.get(vertexId);
	}

}
