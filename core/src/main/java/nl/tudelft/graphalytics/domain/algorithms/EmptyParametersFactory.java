package nl.tudelft.graphalytics.domain.algorithms;

import nl.tudelft.graphalytics.configuration.InvalidConfigurationException;
import org.apache.commons.configuration.Configuration;

/**
 * Default implementation of ParameterFactory that returns null as parsed object.
 *
 * @author Tim Hegeman
 */
public final class EmptyParametersFactory implements ParameterFactory<Object> {
	@Override
	public Object fromConfiguration(Configuration configuration, String baseProperty)
			throws InvalidConfigurationException {
		return null;
	}
}
