package org.tudelft.graphalytics.mapreducev2.stats;

import org.apache.hadoop.util.Tool;
import org.tudelft.graphalytics.mapreducev2.MapReduceJobLauncher;

public class STATSJobLauncher extends MapReduceJobLauncher {

	@Override
	protected Tool createDirectedJob(String input, String intermediate, String output) {
		return new DirectedStatsJob(input, intermediate, output, null);
	}

	@Override
	protected Tool createUndirectedJob(String input, String intermediate, String output) {
		// TODO Auto-generated method stub
		return null;
	}

}
