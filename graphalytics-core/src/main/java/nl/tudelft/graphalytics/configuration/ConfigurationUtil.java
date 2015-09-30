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
import org.apache.commons.configuration.ConversionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigurationUtil {
	private static final Logger LOG = LogManager.getLogger();

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
		try {
			return config.getBoolean(property);
		} catch (ConversionException ignore) {
			throw new InvalidConfigurationException("Invalid value for property \"" + property +
					"\": \"" + config.getString(property) + "\", expected a boolean value.");
		}
	}
	
	public static int getInteger(Configuration config, String property)
			throws InvalidConfigurationException {
		ensureConfigurationKeyExists(config, property);
		try {
			return config.getInt(property);
		} catch (ConversionException ignore) {
			throw new InvalidConfigurationException("Invalid value for property \"" + property +
					"\": \"" + config.getString(property) + "\", expected an integer value.");
		}
	}
	
	public static long getLong(Configuration config, String property)
			throws InvalidConfigurationException {
		ensureConfigurationKeyExists(config, property);
		try {
			return config.getLong(property);
		} catch (ConversionException ignore) {
			throw new InvalidConfigurationException("Invalid value for property \"" + property +
					"\": \"" + config.getString(property) + "\", expected a long value.");
		}
	}

	public static long getLongOrWarn(Configuration config, String property, long defaultValue) {
		try {
			return getLong(config, property);
		} catch (InvalidConfigurationException ex) {
			LOG.warn("Failed to read property \"" + property + "\", defaulting to " + defaultValue + ".", ex);
			return defaultValue;
		}
	}
	
	public static float getFloat(Configuration config, String property)
			throws InvalidConfigurationException {
		ensureConfigurationKeyExists(config, property);
		try {
			return config.getFloat(property);
		} catch (ConversionException ignore) {
			throw new InvalidConfigurationException("Invalid value for property \"" + property +
					"\": \"" + config.getString(property) + "\", expected a floating point value.");
		}
	}

	public static double getDouble(Configuration config, String property)
			throws InvalidConfigurationException {
		ensureConfigurationKeyExists(config, property);
		try {
			return config.getDouble(property);
		} catch (ConversionException ignore) {
			throw new InvalidConfigurationException("Invalid value for property \"" + property +
					"\": \"" + config.getString(property) + "\", expected a double value.");
		}
	}
	
}
