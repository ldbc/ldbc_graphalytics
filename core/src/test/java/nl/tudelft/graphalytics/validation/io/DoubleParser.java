package nl.tudelft.graphalytics.validation.io;

import java.io.IOException;

/**
 * Implementation of GraphValueParser for double values.
 *
 * @author Tim Hegeman
 */
public class DoubleParser implements GraphValueParser<Double> {

	@Override
	public Double parseValue(String token) throws IOException {
		return Double.parseDouble(token);
	}

}
