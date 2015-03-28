package nl.tudelft.graphalytics.validation.io;

/**
 * Utility class for parsing a String token to a value type. Used for loading graphs from files.
 *
* @author Tim Hegeman
*/
public interface GraphValueParser<ValueType> {

	/**
	 * @param token the string representation of a vertex or edge value
	 * @return the parsed vertex or edge value
	 */
	ValueType parseValue(String token);

}
