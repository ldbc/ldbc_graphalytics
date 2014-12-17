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
	
	@Override
	public String toString() {
		return "BFSParameters(" + sourceVertex + ")";
	}
	
	public static BFSParameters parse(String data) {
		if (!data.startsWith("BFSParameters("))
				return null;
		long sourceVertex = Long.parseLong(data.split("[()]")[1]);
		return new BFSParameters(sourceVertex);
	}
}
