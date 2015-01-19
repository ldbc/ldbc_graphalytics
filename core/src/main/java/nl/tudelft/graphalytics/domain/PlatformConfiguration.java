package nl.tudelft.graphalytics.domain;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Configuration details for the benchmarked platform. Supports "inherited" configuration (i.e., having a default
 * configuration with overriding properties), and exposes this inheritance by assigning a "source" to each property's
 * value.
 * <p/>
 * Suggested use of the inheritance feature includes overriding default platform settings with per-benchmark values
 * to highlight which parameters of the platform were changed to enhance performance.
 *
 * @author Tim Hegeman
 */
public class PlatformConfiguration implements Serializable {

	private final Map<String, String> properties;
	private final String sourceName;
	private final PlatformConfiguration baseConfiguration;

	/**
	 * @param properties        a map of properties with corresponding values
	 * @param sourceName        the name to give this "source" of properties
	 * @param baseConfiguration the PlatformConfiguration to "inherit", may be null
	 */
	public PlatformConfiguration(Map<String, String> properties, String sourceName,
	                             PlatformConfiguration baseConfiguration) {
		this.properties = new HashMap<>(properties);
		this.sourceName = sourceName;
		this.baseConfiguration = baseConfiguration;
	}

	/**
	 * @return an empty PlatformConfiguration
	 */
	public static PlatformConfiguration empty() {
		return new PlatformConfiguration(new HashMap<String, String>(), "NOT SET", null);
	}

	/**
	 * @return the set of property names set by this configuration
	 */
	public Set<String> getProperties() {
		return Collections.unmodifiableSet(properties.keySet());
	}

	/**
	 * @param property the name of the property to retrieve
	 * @return the value of the property, or null if not found
	 */
	public String getValueOfProperty(String property) {
		if (properties.containsKey(property))
			return properties.get(property);
		if (baseConfiguration != null)
			return baseConfiguration.getValueOfProperty(property);
		return null;
	}

	/**
	 * @param property the name of the property to retrieve
	 * @return the source of the property, or null if not found
	 */
	public String getSourceOfProperty(String property) {
		if (properties.containsKey(property))
			return sourceName;
		if (baseConfiguration != null)
			return baseConfiguration.getSourceOfProperty(property);
		return null;
	}

	/**
	 * @return the name to give this "source" of properties
	 */
	public String getSourceName() {
		return sourceName;
	}

	/**
	 * @return the inherited PlatformConfiguration
	 */
	public PlatformConfiguration getBaseConfiguration() {
		return baseConfiguration;
	}

}
