package nl.tudelft.graphalytics.domain.algorithms;

import nl.tudelft.graphalytics.configuration.ConfigurationUtil;
import nl.tudelft.graphalytics.configuration.InvalidConfigurationException;
import org.apache.commons.configuration.Configuration;

import java.io.Serializable;

/**
 * Parameters for the execution of the forest fire model algorithm.
 *
 * @author Tim Hegeman
 */
public class ForestFireModelParameters implements Serializable {
	private final long maxId;
	private final float pRatio;
	private final float rRatio;
	private final int maxIterations;
	private final int numNewVertices;

	/**
	 * @param maxId          the highest used vertex ID in the graph
	 * @param pRatio         geometric distribution parameter for the forward burning probability
	 * @param rRatio         geometric distribution parameter for the backward burning probability
	 * @param maxIterations  maximum number of iterations of the forest fire model to execute
	 * @param numNewVertices the number of new vertices to add to the graph
	 */
	public ForestFireModelParameters(long maxId, float pRatio, float rRatio, int maxIterations, int numNewVertices) {
		this.maxId = maxId;
		this.pRatio = pRatio;
		this.rRatio = rRatio;
		this.maxIterations = maxIterations;
		this.numNewVertices = numNewVertices;
	}

	/**
	 * Parses a ForestFireModelParameters object from the properties of a Configuration object.
	 *
	 * @param config      the Configuration describing the ForestFireModelParameters
	 * @param algProperty the name of property describing the ForestFireModelParameters
	 * @return the parsed ForestFireModelParameters
	 * @throws InvalidConfigurationException iff the configuration does not contain the required properties
	 */
	public static ForestFireModelParameters fromConfiguration(Configuration config, String algProperty)
			throws InvalidConfigurationException {
		return new ForestFireModelParameters(ConfigurationUtil.getLong(config, algProperty + ".max-id"),
				ConfigurationUtil.getFloat(config, algProperty + ".pratio"),
				ConfigurationUtil.getFloat(config, algProperty + ".rratio"),
				ConfigurationUtil.getInteger(config, algProperty + ".max-iterations"),
				ConfigurationUtil.getInteger(config, algProperty + ".new-vertices"));
	}

	/**
	 * @return the highest used vertex ID in the graph
	 */
	public long getMaxId() {
		return maxId;
	}

	/**
	 * @return geometric distribution parameter for the forward burning probability
	 */
	public float getPRatio() {
		return pRatio;
	}

	/**
	 * @return geometric distribution parameter for the backward burning probability
	 */
	public float getRRatio() {
		return rRatio;
	}

	/**
	 * @return maximum number of iterations of the forest fire model to execute
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	/**
	 * @return the number of new vertices to add to the graph
	 */
	public int getNumNewVertices() {
		return numNewVertices;
	}

}
