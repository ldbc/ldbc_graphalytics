package nl.tudelft.graphalytics.validation.conn;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Tim Hegeman
 */
public class ConnectedComponentsOutput {

	private final Map<Long, Long> componentIds;

	public ConnectedComponentsOutput(Map<Long, Long> componentIds) {
		this.componentIds = new HashMap<>(componentIds);
	}

	public Set<Long> getVertices() {
		return componentIds.keySet();
	}

	public long getComponentIdForVertex(long vertexId) {
		return componentIds.get(vertexId);
	}

}
