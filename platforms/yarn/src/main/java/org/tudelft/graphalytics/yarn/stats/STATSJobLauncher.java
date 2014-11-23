package org.tudelft.graphalytics.yarn.stats;

import org.apache.hadoop.util.Tool;
import org.tudelft.graphalytics.yarn.YarnJobLauncher;

public class STATSJobLauncher extends YarnJobLauncher {

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
