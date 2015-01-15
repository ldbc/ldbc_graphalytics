package nl.tudelft.graphalytics.mapreducev2.evo;

import nl.tudelft.graphalytics.algorithms.EVOParameters;
import nl.tudelft.graphalytics.mapreducev2.MapReduceJobLauncher;
import nl.tudelft.graphalytics.mapreducev2.MapReduceJob;

/**
 * Job launcher for the forest fire model algorithm on MapReduce version 2.
 *
 * @author Tim Hegeman
 */
public class ForestFireModelJobLauncher extends MapReduceJobLauncher {
	
	private EVOParameters getParameters() {
		assert (parameters instanceof EVOParameters);
		return (EVOParameters) parameters;
	}

	@Override
	protected MapReduceJob<?> createDirectedJob(String input, String intermediate, String output) {
		return new DirectedForestFireModelJob(input, intermediate, output, getParameters());
	}

	@Override
	protected MapReduceJob<?> createUndirectedJob(String input, String intermediate, String output) {
		return new UndirectedForestFireModelJob(input, intermediate, output, getParameters());
	}

}
