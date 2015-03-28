package nl.tudelft.graphalytics.validation.io;

import java.io.IOException;

/**
 * Utility class for parsing a String token to a value type. Used for loading graphs from files.
 *
* @author Tim Hegeman
*/
public interface GraphValueParser<ValueType> {

	ValueType parseValue(String token) throws IOException;

}
