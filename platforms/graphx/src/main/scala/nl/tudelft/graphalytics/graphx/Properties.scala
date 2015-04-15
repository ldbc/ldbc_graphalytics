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
package nl.tudelft.graphalytics.graphx

import org.apache.commons.configuration.{ConfigurationException, PropertiesConfiguration}

/**
 * Utility class for properties files.
 *
 * @author Tim Hegeman
 */
class Properties(configuration: PropertiesConfiguration = new PropertiesConfiguration()) {

	/**
	 * @param property the property to read
	 * @return the value of the property, or None if it does not exist
	 */
	def getString(property: String) =
		if (configuration.containsKey(property))
			Some(configuration.getString(property))
		else
			None

}

object Properties {

	/**
	 * @param filename the file to read properties from
	 * @return a new Properties object, or None if the file could not be read
	 */
	def fromFile(filename: String): Option[Properties] = {
		try {
			Some(new Properties(new PropertiesConfiguration(filename)))
		} catch {
			case e: ConfigurationException => {
				System.err.println(s"Could not find properties file: $filename")
				None
			}
		}
	}

	/**
	 * @return an empty Properties object
	 */
	def empty() = new Properties
}
