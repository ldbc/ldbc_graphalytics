package nl.tudelft.graphalytics.algorithms;

import org.apache.commons.configuration.Configuration;
import nl.tudelft.graphalytics.configuration.ConfigurationUtil;
import nl.tudelft.graphalytics.configuration.InvalidConfigurationException;

public class STATSParameters {

	private long collectionNode;
	
	public STATSParameters(long collectionNode) {
		this.collectionNode = collectionNode;
	}
	
	public long getCollectionNode() {
		return collectionNode;
	}
	
	public static STATSParameters fromConfiguration(Configuration config, String algProperty)
			throws InvalidConfigurationException {
		return new STATSParameters(ConfigurationUtil.getLong(config, algProperty + ".collection-node"));
	}
}
