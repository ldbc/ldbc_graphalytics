package org.tudelft.graphalytics.mapreducev2.stats;

import org.tudelft.graphalytics.mapreducev2.MapReduceJobLauncher;
import org.tudelft.graphalytics.mapreducev2.MapReduceJob;

public class STATSJobLauncher extends MapReduceJobLauncher {

	@Override
	protected MapReduceJob<?> createDirectedJob(String input, String intermediate, String output) {
		return new DirectedStatsJob(input, intermediate, output, null);
	}

	@Override
	protected MapReduceJob<?> createUndirectedJob(String input, String intermediate, String output) {
		return new UndirectedStatsJob(input, intermediate, output, null);
	}

}
