package nl.tudelft.graphalytics.domain;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Configuration details describing the system on which the benchmarked platform runs.
 *
 * @author Tim Hegeman
 */
public class SystemConfiguration implements Serializable {

	private final Map<String, String> properties;

	/**
	 * @param properties map of properties and corresponding values describing the system
	 */
	public SystemConfiguration(Map<String, String> properties) {
		this.properties = properties;
	}

	/**
	 * @return a SystemConfiguration without properties
	 */
	public static SystemConfiguration empty() {
		return new SystemConfiguration(Collections.<String, String>emptyMap());
	}

	/**
	 * @return map of properties and corresponding values describing the system
	 */
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}
}
