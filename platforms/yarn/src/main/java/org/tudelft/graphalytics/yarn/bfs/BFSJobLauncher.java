package org.tudelft.graphalytics.yarn.bfs;

import org.apache.hadoop.util.Tool;
import org.tudelft.graphalytics.algorithms.BFSParameters;
import org.tudelft.graphalytics.yarn.YarnJobLauncher;

public class BFSJobLauncher extends YarnJobLauncher {
	public static final String SOURCE_VERTEX_KEY = "BFS.source";

    private BFSParameters getParameters() {
    	assert (parameters instanceof BFSParameters);
    	return (BFSParameters)parameters;
    }

	@Override
	protected Tool createDirectedJob(String input, String intermediate, String output) {
		return new DirectedBFSJob(input, intermediate, output, getParameters().getSourceVertex());
	}

	@Override
	protected Tool createUndirectedJob(String input, String intermediate, String output) {
		return new UndirectedBFSJob(input, intermediate, output, getParameters().getSourceVertex());
	}
}
