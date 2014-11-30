package org.tudelft.graphalytics.mapreducev2.stats;

import org.tudelft.graphalytics.mapreducev2.MapReduceJobLauncher;
import org.tudelft.graphalytics.mapreducev2.ToolRunnerJob;

public class STATSJobLauncher extends MapReduceJobLauncher {

	@Override
	protected ToolRunnerJob<?> createDirectedJob(String input, String intermediate, String output) {
		return new DirectedStatsJob(input, intermediate, output, null);
	}

	@Override
	protected ToolRunnerJob<?> createUndirectedJob(String input, String intermediate, String output) {
		// TODO Auto-generated method stub
		return null;
	}

}
