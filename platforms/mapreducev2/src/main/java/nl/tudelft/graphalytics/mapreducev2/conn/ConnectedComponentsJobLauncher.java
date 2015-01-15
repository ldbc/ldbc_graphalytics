package nl.tudelft.graphalytics.mapreducev2.conn;

import nl.tudelft.graphalytics.mapreducev2.MapReduceJobLauncher;
import nl.tudelft.graphalytics.mapreducev2.MapReduceJob;

/**
 * Job launcher for the connected components algorithm on MapReduce version 2.
 *
 * @author Tim Hegeman
 */
public class ConnectedComponentsJobLauncher extends MapReduceJobLauncher {

	@Override
	protected MapReduceJob<?> createDirectedJob(String input, String intermediate, String output) {
		return new ConnectedComponentsJob(input, intermediate, output, null, true);
	}

	@Override
	protected MapReduceJob<?> createUndirectedJob(String input, String intermediate, String output) {
		return new ConnectedComponentsJob(input, intermediate, output, null, false);
	}
	
}
