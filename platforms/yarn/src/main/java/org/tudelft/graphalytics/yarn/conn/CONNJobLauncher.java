package org.tudelft.graphalytics.yarn.conn;

import org.apache.hadoop.util.Tool;
import org.tudelft.graphalytics.yarn.YarnJobLauncher;

public class CONNJobLauncher extends YarnJobLauncher {
	// Stopping condition
	public enum Label {
        UPDATED
    }

	@Override
	protected Tool createDirectedJob(String input, String intermediate, String output) {
		return new LabelDirectedConnectedComponentsJob(input, intermediate, output);
	}

	@Override
	protected Tool createUndirectedJob(String input, String intermediate, String output) {
		return new LabelUndirectedConnectedComponentsJob(input, intermediate, output);
	}
	
}
