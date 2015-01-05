package org.tudelft.graphalytics.mapreducev2.cd;

import org.tudelft.graphalytics.algorithms.CDParameters;
import org.tudelft.graphalytics.mapreducev2.MapReduceJobLauncher;
import org.tudelft.graphalytics.mapreducev2.MapReduceJob;

public class CDJobLauncher extends MapReduceJobLauncher {
	// Stopping condition
    public enum Label {
        CHANGED
    }
	
    public static final String NODE_PREFERENCE = "CD.NodePreference";
    public static final String HOP_ATTENUATION = "CD.HopAttenuation";

    private CDParameters getParameters() {
    	assert (parameters instanceof CDParameters);
    	return (CDParameters)parameters;
    }

	@Override
	protected MapReduceJob<?> createDirectedJob(String input, String intermediate, String output) {
		return new CommunityDetectionJob(input, intermediate, output, getParameters(), true);
	}

	@Override
	protected MapReduceJob<?> createUndirectedJob(String input, String intermediate, String output) {
		return new CommunityDetectionJob(input, intermediate, output, getParameters(), false);
	}
    
}
