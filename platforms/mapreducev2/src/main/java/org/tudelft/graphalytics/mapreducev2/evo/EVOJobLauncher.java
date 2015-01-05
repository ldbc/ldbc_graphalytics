package org.tudelft.graphalytics.mapreducev2.evo;

import org.tudelft.graphalytics.algorithms.EVOParameters;
import org.tudelft.graphalytics.mapreducev2.MapReduceJobLauncher;
import org.tudelft.graphalytics.mapreducev2.MapReduceJob;

public class EVOJobLauncher extends MapReduceJobLauncher {
	
	private EVOParameters getParameters() {
		assert (parameters instanceof EVOParameters);
		return (EVOParameters) parameters;
	}

	@Override
	protected MapReduceJob<?> createDirectedJob(String input, String intermediate, String output) {
		return new DirectedFFMJob(input, intermediate, output, getParameters());
	}

	@Override
	protected MapReduceJob<?> createUndirectedJob(String input, String intermediate, String output) {
		return new UndirectedFFMJob(input, intermediate, output, getParameters());
	}

}
