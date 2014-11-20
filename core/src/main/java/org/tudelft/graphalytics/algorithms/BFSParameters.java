package org.tudelft.graphalytics.algorithms;

import org.apache.commons.configuration.Configuration;
import org.tudelft.graphalytics.configuration.InvalidConfigurationException;
import org.tudelft.graphalytics.configuration.ConfigurationUtil;

public class BFSParameters {
	private final String sourceVertex;
	
	public BFSParameters(String sourceVertex) {
		this.sourceVertex = sourceVertex;
	}
	
	public String getSourceVertex() {
		return sourceVertex;
	}

	public static BFSParameters fromConfiguration(Configuration config, String algProperty)
			throws InvalidConfigurationException {
		return new BFSParameters(ConfigurationUtil.getString(config, algProperty + ".source-vertex"));
	}
}
