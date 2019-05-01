/*
 * Copyright 2015 - 2017 Atlarge Research Team,
 * operating at Technische Universiteit Delft
 * and Vrije Universiteit Amsterdam, the Netherlands.
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
package science.atlarge.graphalytics.configuration;

import org.apache.commons.configuration.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for parsing the benchmark configuration. The get-functions throw a descriptive exception if the
 * requested property does not exist or contains a value incompatible with the requested type.
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public final class ConfigurationUtil {

	private static final Logger LOG = LogManager.getLogger();

	/**
	 * Prevent instantiation of utility class.
	 */
	private ConfigurationUtil() {
	}

	public static Configuration loadConfiguration(String fileName) {

		Configuration configuration = null;
		try {
			configuration = new PropertiesConfiguration(fileName);
		} catch (ConfigurationException e) {
			throw new InvalidConfigurationException("Cannot retrieve properties from file: " + fileName);
		}
		return configuration;
	}

	public static void ensureConfigurationKeyExists(Configuration config, String property)
			throws InvalidConfigurationException {
		if (!config.containsKey(property)) {
			throw new InvalidConfigurationException("Missing property: \"" + resolve(config, property) + "\".");
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
			throw new InvalidConfigurationException("Invalid value for property \"" + resolve(config, property) +
					"\": \"" + config.getString(property) + "\", expected a boolean value.");
		}
	}

	public static boolean getBooleanIfExists(Configuration config, String property) {
		if (config.containsKey(property)) {
			return config.getBoolean(property);
		}

		return false;
	}
	
	public static int getInteger(Configuration config, String property)
			throws InvalidConfigurationException {
		ensureConfigurationKeyExists(config, property);
		try {
			return config.getInt(property);
		} catch (ConversionException ignore) {
			throw new InvalidConfigurationException("Invalid value for property \"" + resolve(config, property) +
					"\": \"" + config.getString(property) + "\", expected an integer value.");
		}
	}
	
	public static long getLong(Configuration config, String property)
			throws InvalidConfigurationException {
		ensureConfigurationKeyExists(config, property);
		try {
			return config.getLong(property);
		} catch (ConversionException ignore) {
			throw new InvalidConfigurationException("Invalid value for property \"" + resolve(config, property) +
					"\": \"" + config.getString(property) + "\", expected a long value.");
		}
	}

	public static long getLongOrWarn(Configuration config, String property, long defaultValue) {
		try {
			return getLong(config, property);
		} catch (InvalidConfigurationException ex) {
			LOG.warn("Failed to read property \"" + resolve(config, property) + "\", defaulting to " +
					defaultValue + ".", ex);
			return defaultValue;
		}
	}
	
	public static float getFloat(Configuration config, String property)
			throws InvalidConfigurationException {
		ensureConfigurationKeyExists(config, property);
		try {
			return config.getFloat(property);
		} catch (ConversionException ignore) {
			throw new InvalidConfigurationException("Invalid value for property \"" + resolve(config, property) +
					"\": \"" + config.getString(property) + "\", expected a floating point value.");
		}
	}

	public static double getDouble(Configuration config, String property)
			throws InvalidConfigurationException {
		ensureConfigurationKeyExists(config, property);
		try {
			return config.getDouble(property);
		} catch (ConversionException ignore) {
			throw new InvalidConfigurationException("Invalid value for property \"" + resolve(config, property) +
					"\": \"" + config.getString(property) + "\", expected a double value.");
		}
	}
	
	public static char getChar(Configuration config, String property) throws InvalidConfigurationException {
		String stringValue = getString(config, property);
		if (stringValue.length() == 1) {
			return stringValue.charAt(0);
		} else if (stringValue.length() == 3 && stringValue.charAt(0) == stringValue.charAt(2) &&
				(stringValue.charAt(0) == '\"' || stringValue.charAt(0) == '\'')) {
			// Extract char when surrounded by quotes
			return stringValue.charAt(1);
		} else {
			throw new InvalidConfigurationException("Invalid value for property \"" + resolve(config, property) +
					"\": \"" + config.getString(property) + "\", expected a single character.");
		}
	}

	public static String resolve(Configuration config, String property) {
		String fullProperty = property;
		while (config instanceof SubsetConfiguration) {
			fullProperty = ((SubsetConfiguration)config).getPrefix() + "." + fullProperty;
			config = ((SubsetConfiguration)config).getParent();
		}
		return fullProperty;
	}

}
