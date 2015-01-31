package nl.tudelft.graphalytics.domain.algorithms;

import nl.tudelft.graphalytics.configuration.ConfigurationUtil;
import nl.tudelft.graphalytics.configuration.InvalidConfigurationException;
import org.apache.commons.configuration.Configuration;

import java.io.Serializable;

/**
 * Parameters for the execution of the breadth first search algorithm.
 *
 * @author Tim Hegeman
 */
public final class BreadthFirstSearchParameters implements Serializable {
	private final long sourceVertex;

	/**
	 * @param sourceVertex the source of the breadth first search
	 */
	public BreadthFirstSearchParameters(long sourceVertex) {
		this.sourceVertex = sourceVertex;
	}

	/**
	 * Parses a BreadthFirstSearchParameters object from the properties of a Configuration object.
	 *
	 * @param config      the Configuration describing the BreadthFirstSearchParameters
	 * @param algProperty the name of property describing the BreadthFirstSearchParameters
	 * @return the parsed BreadthFirstSearchParameters
	 * @throws InvalidConfigurationException iff the configuration does not contain the required properties
	 */
	public static BreadthFirstSearchParameters fromConfiguration(Configuration config, String algProperty)
			throws InvalidConfigurationException {
		return new BreadthFirstSearchParameters(ConfigurationUtil.getLong(config, algProperty + ".source-vertex"));
	}

	/**
	 * @return the source of the breadth first search
	 */
	public long getSourceVertex() {
		return sourceVertex;
	}

	@Override
	public String toString() {
		return "BreadthFirstSearchParameters(" + sourceVertex + ")";
	}

	/**
	 * Factory for parsing a BreadthFirstSearchParameters object from the properties of a Configuration object.
	 */
	public static final class BreadthFirstSearchParametersFactory implements
			ParameterFactory<BreadthFirstSearchParameters> {
		@Override
		public BreadthFirstSearchParameters fromConfiguration(Configuration configuration, String baseProperty)
				throws InvalidConfigurationException {
			return new BreadthFirstSearchParameters(
					ConfigurationUtil.getLong(configuration, baseProperty + ".source-vertex"));
		}
	}
}
