package nl.tudelft.graphalytics.domain;

import java.io.Serializable;

/**
 * Platform-dependent information regarding the results of a single benchmark run.
 *
 * @author Tim Hegeman
 */
public class PlatformBenchmarkResult implements Serializable {

	private final PlatformConfiguration platformConfiguration;

	/**
	 * @param platformConfiguration platform-specific configuration used for this benchmark
	 */
	public PlatformBenchmarkResult(PlatformConfiguration platformConfiguration) {
		this.platformConfiguration = platformConfiguration;
	}

	/**
	 * @return platform-specific configuration used for this benchmark
	 */
	public PlatformConfiguration getPlatformConfiguration() {
		return platformConfiguration;
	}

}