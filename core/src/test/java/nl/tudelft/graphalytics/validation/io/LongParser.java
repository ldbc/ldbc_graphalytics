package nl.tudelft.graphalytics.validation.io;

import java.io.IOException;

/**
 * Implementation of GraphValueParser for long values.
 *
 * @author Tim Hegeman
 */
public class LongParser implements GraphValueParser<Long> {

	@Override
	public Long parseValue(String token) throws IOException {
		return Long.parseLong(token);
	}

}
