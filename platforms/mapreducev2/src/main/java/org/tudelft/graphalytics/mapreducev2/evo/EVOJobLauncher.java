package org.tudelft.graphalytics.mapreducev2.evo;

import org.apache.hadoop.util.Tool;
import org.tudelft.graphalytics.algorithms.EVOParameters;
import org.tudelft.graphalytics.mapreducev2.MapReduceJobLauncher;

public class EVOJobLauncher extends MapReduceJobLauncher {
	
	private EVOParameters getParameters() {
		assert (parameters instanceof EVOParameters);
		return (EVOParameters) parameters;
	}

	@Override
	protected Tool createDirectedJob(String input, String intermediate, String output) {
		return new DirectedFFMJob(input, intermediate, output, getParameters());
	}

	@Override
	protected Tool createUndirectedJob(String input, String intermediate, String output) {
		return new UndirectedFFMJob(input, intermediate, output, getParameters());
	}

}
