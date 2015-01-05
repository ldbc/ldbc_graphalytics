package org.tudelft.graphalytics.mapreducev2.conn;

import org.tudelft.graphalytics.mapreducev2.MapReduceJobLauncher;
import org.tudelft.graphalytics.mapreducev2.MapReduceJob;

public class CONNJobLauncher extends MapReduceJobLauncher {
	// Stopping condition
	public enum Label {
        UPDATED
    }

	@Override
	protected MapReduceJob<?> createDirectedJob(String input, String intermediate, String output) {
		return new ConnectedComponentsJob(input, intermediate, output, null, true);
	}

	@Override
	protected MapReduceJob<?> createUndirectedJob(String input, String intermediate, String output) {
		return new ConnectedComponentsJob(input, intermediate, output, null, false);
	}
	
}
