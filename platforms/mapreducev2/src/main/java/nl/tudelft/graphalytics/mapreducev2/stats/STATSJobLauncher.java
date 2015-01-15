package nl.tudelft.graphalytics.mapreducev2.stats;

import nl.tudelft.graphalytics.mapreducev2.MapReduceJobLauncher;
import nl.tudelft.graphalytics.mapreducev2.MapReduceJob;

/**
 * @author Tim Hegeman
 */
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
