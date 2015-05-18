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

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Configuration details describing the system on which the benchmarked platform runs.
 *
 * @author Tim Hegeman
 */
public final class SystemDetails implements Serializable {

	private final Map<String, String> properties;

	/**
	 * @param properties map of properties and corresponding values describing the system
	 */
	public SystemDetails(Map<String, String> properties) {
		this.properties = properties;
	}

	/**
	 * @return a SystemConfiguration without properties
	 */
	public static SystemDetails empty() {
		return new SystemDetails(Collections.<String, String>emptyMap());
	}

	/**
	 * @return map of properties and corresponding values describing the system
	 */
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}
}
