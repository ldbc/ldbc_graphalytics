package org.tudelft.graphalytics.mapreducev2.cd;

import org.tudelft.graphalytics.algorithms.CDParameters;
import org.tudelft.graphalytics.mapreducev2.MapReduceJobLauncher;
import org.tudelft.graphalytics.mapreducev2.MapReduceJob;

/**
 * Job launcher for the community detection algorithm on MapReduce version 2.
 *
 * @author Tim Hegeman
 */
public class CommunityDetectionJobLauncher extends MapReduceJobLauncher {
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
