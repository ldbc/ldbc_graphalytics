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
