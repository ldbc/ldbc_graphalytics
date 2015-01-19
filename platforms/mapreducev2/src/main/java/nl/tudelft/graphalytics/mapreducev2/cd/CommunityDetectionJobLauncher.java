package nl.tudelft.graphalytics.mapreducev2.cd;

import nl.tudelft.graphalytics.domain.algorithms.CommunityDetectionParameters;
import nl.tudelft.graphalytics.mapreducev2.MapReduceJobLauncher;
import nl.tudelft.graphalytics.mapreducev2.MapReduceJob;

/**
 * Job launcher for the community detection algorithm on MapReduce version 2.
 *
 * @author Tim Hegeman
 */
public class CommunityDetectionJobLauncher extends MapReduceJobLauncher {
	private CommunityDetectionParameters getParameters() {
    	assert (parameters instanceof CommunityDetectionParameters);
    	return (CommunityDetectionParameters)parameters;
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
