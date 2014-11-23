package org.tudelft.graphalytics.mapreducev2.conn;

import org.apache.hadoop.util.Tool;
import org.tudelft.graphalytics.mapreducev2.MapReduceJobLauncher;

public class CONNJobLauncher extends MapReduceJobLauncher {
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
