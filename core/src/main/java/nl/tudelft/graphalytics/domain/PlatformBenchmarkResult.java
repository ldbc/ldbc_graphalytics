package nl.tudelft.graphalytics.domain;

import java.io.Serializable;

/**
 * Platform-dependent information regarding the results of a single benchmark run.
 *
 * @author Tim Hegeman
 */
public final class PlatformBenchmarkResult implements Serializable {

	private final NestedConfiguration platformConfiguration;

	/**
	 * @param platformConfiguration platform-specific configuration used for this benchmark
	 */
	public PlatformBenchmarkResult(NestedConfiguration platformConfiguration) {
		this.platformConfiguration = platformConfiguration;
	}

	/**
	 * @return platform-specific configuration used for this benchmark
	 */
	public NestedConfiguration getPlatformConfiguration() {
		return platformConfiguration;
	}

}