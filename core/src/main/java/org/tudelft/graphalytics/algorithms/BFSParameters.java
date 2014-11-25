package org.tudelft.graphalytics.algorithms;

import org.apache.commons.configuration.Configuration;
import org.tudelft.graphalytics.configuration.InvalidConfigurationException;
import org.tudelft.graphalytics.configuration.ConfigurationUtil;

public class BFSParameters {
	private final long sourceVertex;
	
	public BFSParameters(long sourceVertex) {
		this.sourceVertex = sourceVertex;
	}
	
	public long getSourceVertex() {
		return sourceVertex;
	}

	public static BFSParameters fromConfiguration(Configuration config, String algProperty)
			throws InvalidConfigurationException {
		return new BFSParameters(ConfigurationUtil.getLong(config, algProperty + ".source-vertex"));
	}
}
