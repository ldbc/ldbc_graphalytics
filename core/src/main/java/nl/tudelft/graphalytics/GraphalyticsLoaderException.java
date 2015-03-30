package nl.tudelft.graphalytics;

/**
 * Wrapper class for exceptions that occur during the initialization phase of Graphalytics.
 *
 * @author Tim Hegeman
 */
public class GraphalyticsLoaderException extends RuntimeException {

	public GraphalyticsLoaderException(String message) {
		super(message);
	}

	public GraphalyticsLoaderException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
