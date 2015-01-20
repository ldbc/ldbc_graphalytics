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

	/**
	 * Factory for parsing a CommunityDetectionParameters object from the properties of a Configuration object.
	 */
	public static final class CommunityDetectionParametersFactory implements
			ParameterFactory<CommunityDetectionParameters> {
		@Override
		public CommunityDetectionParameters fromConfiguration(Configuration configuration, String baseProperty)
				throws InvalidConfigurationException {
			return new CommunityDetectionParameters(
					ConfigurationUtil.getFloat(configuration, baseProperty + ".node-preference"),
					ConfigurationUtil.getFloat(configuration, baseProperty + ".hop-attenuation"),
					ConfigurationUtil.getInteger(configuration, baseProperty + ".max-iterations"));
		}
	}
}
