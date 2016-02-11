package nl.tudelft.graphalytics;

import nl.tudelft.graphalytics.domain.NestedConfiguration;

/**
 * Partial implementation of the Platform interface that provides default no-op implementations for non-essential
 * methods.
 *
 * @author Tim Hegeman
 */
public abstract class AbstractPlatform implements Platform {

	@Override
	public NestedConfiguration getPlatformConfiguration() {
		return NestedConfiguration.empty();
	}

}
