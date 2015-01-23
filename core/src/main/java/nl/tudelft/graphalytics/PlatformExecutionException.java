package nl.tudelft.graphalytics;

/**
 * Exception class for wrapping execution failures in algorithm execution.
 *
 * @author Tim Hegeman
 */
public class PlatformExecutionException extends Exception {

	public PlatformExecutionException(String message) {
		super(message);
	}

	public PlatformExecutionException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
