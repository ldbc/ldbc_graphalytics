package org.tudelft.graphalytics.yarn.evo;

import org.apache.hadoop.util.Tool;
import org.tudelft.graphalytics.algorithms.EVOParameters;
import org.tudelft.graphalytics.yarn.YarnJobLauncher;

public class EVOJobLauncher extends YarnJobLauncher {
	
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
