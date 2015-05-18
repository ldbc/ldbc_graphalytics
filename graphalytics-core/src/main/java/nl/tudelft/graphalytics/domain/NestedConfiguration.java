/**
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics.domain;

import org.apache.commons.configuration.Configuration;

import java.io.Serializable;
import java.util.*;

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
public final class NestedConfiguration implements Serializable {

	private final Map<String, String> properties;
	private final String sourceName;
	private final NestedConfiguration baseConfiguration;

	/**
	 * @param properties        a map of properties with corresponding values
	 * @param sourceName        the name to give this "source" of properties
	 * @param baseConfiguration the PlatformConfiguration to "inherit", may be null
	 */
	public NestedConfiguration(Map<String, String> properties, String sourceName,
	                           NestedConfiguration baseConfiguration) {
		this.properties = new HashMap<>(properties);
		this.sourceName = sourceName;
		this.baseConfiguration = baseConfiguration;
	}

	/**
	 * @return an empty PlatformConfiguration
	 */
	public static NestedConfiguration empty() {
		return new NestedConfiguration(new HashMap<String, String>(), "NOT SET", null);
	}

	/**
	 * Parses an external configuration and wraps its keys and values in a NestedConfiguration object.
	 *
	 * @param configuration the external configuration to read
	 * @param sourceName    the name to give this "source" of properties
	 * @return the parsed NestedConfiguration
	 */
	public static NestedConfiguration fromExternalConfiguration(Configuration configuration, String sourceName) {
		Map<String, String> properties = new HashMap<>();
		for (Iterator<String> propertyKeys = configuration.getKeys(); propertyKeys.hasNext(); ) {
			String propertyKey = propertyKeys.next();
			properties.put(propertyKey, configuration.getString(propertyKey));
		}
		return new NestedConfiguration(properties, sourceName, null);
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
	public NestedConfiguration getBaseConfiguration() {
		return baseConfiguration;
	}

}
