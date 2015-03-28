package nl.tudelft.graphalytics.validation.io;

/**
 * Implementation of GraphValueParser for long values.
 *
 * @author Tim Hegeman
 */
public class LongParser implements GraphValueParser<Long> {

	@Override
	public Long parseValue(String token) {
		return Long.parseLong(token);
	}

}
