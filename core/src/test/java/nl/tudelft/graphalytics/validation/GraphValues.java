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

	public GraphValues(Map<Long, ValueType> vertexValues) {
		this.vertexValues = new HashMap<>(vertexValues);
	}

	public Set<Long> getVertices() {
		return vertexValues.keySet();
	}

	public ValueType getVertexValue(long vertexId) {
		return vertexValues.get(vertexId);
	}

}
