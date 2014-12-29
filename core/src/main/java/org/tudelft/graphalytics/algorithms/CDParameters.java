package org.tudelft.graphalytics.algorithms;

import org.apache.commons.configuration.Configuration;
import org.tudelft.graphalytics.configuration.InvalidConfigurationException;
import org.tudelft.graphalytics.configuration.ConfigurationUtil;

import java.io.Serializable;

public class CDParameters implements Serializable {
	private static final long serialVersionUID = -810803301798795823L;

	private final float nodePreference;
	private final float hopAttenuation;
	private final int maxIterations;
	
	public CDParameters(float nodePreference, float hopAttenuation, int maxIterations) {
		this.nodePreference = nodePreference;
		this.hopAttenuation = hopAttenuation;
		this.maxIterations = maxIterations;
	}
	
	public float getNodePreference() {
		return nodePreference;
	}
	
	public float getHopAttenuation() {
		return hopAttenuation;
	}
	
	public int getMaxIterations() {
		return maxIterations;
	}
	
	public static CDParameters fromConfiguration(Configuration config, String algProperty)
			throws InvalidConfigurationException {
		return new CDParameters(ConfigurationUtil.getFloat(config, algProperty + ".node-preference"),
				ConfigurationUtil.getFloat(config, algProperty + ".hop-attenuation"),
				ConfigurationUtil.getInteger(config, algProperty + ".max-iterations"));
	}
}
