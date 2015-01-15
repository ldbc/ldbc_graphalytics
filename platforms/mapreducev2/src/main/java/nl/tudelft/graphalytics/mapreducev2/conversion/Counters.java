package nl.tudelft.graphalytics.mapreducev2.conversion;

/**
 * Collection of MapReduce Counters used for providing information to users,
 * and for debugging input parsing.
 *
 * @author Tim Hegeman
 */
public class Counters {

	public enum ParseErrors {
		NUMBER_FORMAT_EXCEPTION,
		INVALID_LINE_FORMAT
	}
	
}
