package nl.tudelft.graphalytics.domain.algorithms;

import nl.tudelft.graphalytics.configuration.ConfigurationUtil;
import nl.tudelft.graphalytics.configuration.InvalidConfigurationException;
import org.apache.commons.configuration.Configuration;

import java.io.Serializable;

/**
 * Parameters for the execution of the community detection algorithm, based on label propagation.
 *
 * @author Tim Hegeman
 */
public class CommunityDetectionParameters implements Serializable {
	private final float nodePreference;
	private final float hopAttenuation;
	private final int maxIterations;

	/**
	 * @param nodePreference the node preference to use for the label propagation
	 * @param hopAttenuation the hop attenuation to use for the label propagation
	 * @param maxIterations  the maximum number of iterations of label propagation to execute
	 */
	public CommunityDetectionParameters(float nodePreference, float hopAttenuation, int maxIterations) {
		this.nodePreference = nodePreference;
		this.hopAttenuation = hopAttenuation;
		this.maxIterations = maxIterations;
	}

	/**
	 * Parses a CommunityDetectionParameters object from the properties of a Configuration object.
	 *
	 * @param config      the Configuration describing the CommunityDetectionParameters
	 * @param algProperty the name of property describing the CommunityDetectionParameters
	 * @return the parsed CommunityDetectionParameters
	 * @throws InvalidConfigurationException iff the configuration does not contain the required properties
	 */
	public static CommunityDetectionParameters fromConfiguration(Configuration config, String algProperty)
			throws InvalidConfigurationException {
		return new CommunityDetectionParameters(ConfigurationUtil.getFloat(config, algProperty + ".node-preference"),
				ConfigurationUtil.getFloat(config, algProperty + ".hop-attenuation"),
				ConfigurationUtil.getInteger(config, algProperty + ".max-iterations"));
	}

	/**
	 * @return the node preference to use for the label propagation
	 */
	public float getNodePreference() {
		return nodePreference;
	}

	/**
	 * @return the hop attenuation to use for the label propagation
	 */
	public float getHopAttenuation() {
		return hopAttenuation;
	}

	/**
	 * @return the maximum number of iterations of label propagation to execute
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	@Override
	public String toString() {
		return "CommunityDetectionParameters(" + nodePreference + "," + hopAttenuation + "," + maxIterations + ")";
	}
}
