package nl.tudelft.graphalytics.validation.io;

/**
 * Implementation of GraphValueParser for double values.
 *
 * @author Tim Hegeman
 */
public class DoubleParser implements GraphValueParser<Double> {

	@Override
	public Double parseValue(String token) {
		return Double.parseDouble(token);
	}

}
