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
package nl.tudelft.graphalytics.configuration;

import org.apache.commons.configuration.Configuration;

public class ConfigurationUtil {

	public static void ensureConfigurationKeyExists(Configuration config, String property)
			throws InvalidConfigurationException {
		if (!config.containsKey(property)) {
			throw new InvalidConfigurationException("Missing property: \"" + property + "\".");
		}
	}
	
	public static String getString(Configuration config, String property)
			throws InvalidConfigurationException {
		ensureConfigurationKeyExists(config, property);
		return config.getString(property);
	}
	
	public static String[] getStringArray(Configuration config, String property)
			throws InvalidConfigurationException {
		ensureConfigurationKeyExists(config, property);
		return config.getStringArray(property);
	}
	
	public static boolean getBoolean(Configuration config, String property)
			throws InvalidConfigurationException {
		ensureConfigurationKeyExists(config, property);
		Boolean value = config.getBoolean(property, null);
		if (value == null) {
			throw new InvalidConfigurationException("Invalid value for property \"" + property +
					"\": \"" + config.getString(property) + "\", expected a boolean value.");
		}
		return value.booleanValue();
	}
	
	public static int getInteger(Configuration config, String property)
			throws InvalidConfigurationException {
		ensureConfigurationKeyExists(config, property);
		Integer value = config.getInteger(property, null);
		if (value == null) {
			throw new InvalidConfigurationException("Invalid value for property \"" + property +
					"\": \"" + config.getString(property) + "\", expected an integer value.");
		}
		return value.intValue();
	}
	
	public static long getLong(Configuration config, String property)
			throws InvalidConfigurationException {
		ensureConfigurationKeyExists(config, property);
		Long value = config.getLong(property, null);
		if (value == null) {
			throw new InvalidConfigurationException("Invalid value for property \"" + property +
					"\": \"" + config.getString(property) + "\", expected a long value.");
		}
		return value.longValue();
	}
	
	public static float getFloat(Configuration config, String property)
			throws InvalidConfigurationException {
		ensureConfigurationKeyExists(config, property);
		Float value = config.getFloat(property, null);
		if (value == null) {
			throw new InvalidConfigurationException("Invalid value for property \"" + property +
					"\": \"" + config.getString(property) + "\", expected a floating point value.");
		}
		return value.floatValue();
	}
	
}
