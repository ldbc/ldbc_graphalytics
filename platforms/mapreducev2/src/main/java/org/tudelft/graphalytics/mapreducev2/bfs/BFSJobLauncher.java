package org.tudelft.graphalytics.mapreducev2.bfs;

import org.apache.hadoop.util.Tool;
import org.tudelft.graphalytics.algorithms.BFSParameters;
import org.tudelft.graphalytics.mapreducev2.MapReduceJobLauncher;

public class BFSJobLauncher extends MapReduceJobLauncher {
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
