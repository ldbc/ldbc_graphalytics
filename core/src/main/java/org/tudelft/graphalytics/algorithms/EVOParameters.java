package org.tudelft.graphalytics.algorithms;

import org.apache.commons.configuration.Configuration;
import org.tudelft.graphalytics.configuration.InvalidConfigurationException;
import org.tudelft.graphalytics.configuration.ConfigurationUtil;

public class EVOParameters {

	private long maxId;
	private float pRatio;
	private float rRatio;
	private int maxIterations;
	private int numNewVertices;
	
	public EVOParameters(long maxId, float pRatio, float rRatio,  int maxIterations, int numNewVertices) {
		this.maxId = maxId;
		this.pRatio = pRatio;
		this.rRatio = rRatio;
		this.maxIterations = maxIterations;
		this.numNewVertices = numNewVertices;
	}
	
	public long getMaxId() {
		return maxId;
	}
	public float getPRatio() {
		return pRatio;
	}
	public float getRRatio() {
		return rRatio;
	}
	public int getMaxIterations() {
		return maxIterations;
	}
	public int getNumNewVertices() {
		return numNewVertices;
	}
	
	public static EVOParameters fromConfiguration(Configuration config, String algProperty)
			throws InvalidConfigurationException {
		return new EVOParameters(ConfigurationUtil.getLong(config, algProperty + ".max-id"),
				ConfigurationUtil.getFloat(config, algProperty + ".pratio"),
				ConfigurationUtil.getFloat(config, algProperty + ".rratio"),
				ConfigurationUtil.getInteger(config, algProperty + ".max-iterations"),
				ConfigurationUtil.getInteger(config, algProperty + ".new-vertices"));
	}
	
}
