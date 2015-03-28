package nl.tudelft.graphalytics.validation.conn;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Container for output of the connected components algorithm, used by Graphalytics validation test.
 *
 * @author Tim Hegeman
 */
public class ConnectedComponentsOutput {

	private final Map<Long, Long> componentIds;

	/**
	 * @param componentIds a map containing the component id of each vertex
	 */
	public ConnectedComponentsOutput(Map<Long, Long> componentIds) {
		this.componentIds = new HashMap<>(componentIds);
	}

	/**
	 * @return a set of vertex ids for which the component id is known
	 */
	public Set<Long> getVertices() {
		return componentIds.keySet();
	}

	/**
	 * @param vertexId the id of a vertex in the output data
	 * @return the corresponding component id
	 */
	public long getComponentIdForVertex(long vertexId) {
		return componentIds.get(vertexId);
	}

}
