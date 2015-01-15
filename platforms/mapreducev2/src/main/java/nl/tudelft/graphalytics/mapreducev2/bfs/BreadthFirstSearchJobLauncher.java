package nl.tudelft.graphalytics.mapreducev2.bfs;

import nl.tudelft.graphalytics.algorithms.BFSParameters;
import nl.tudelft.graphalytics.mapreducev2.MapReduceJobLauncher;
import nl.tudelft.graphalytics.mapreducev2.MapReduceJob;

/**
 * Job launcher for the breadth-first search algorithm on MapReduce version 2.
 *
 * @author Tim Hegeman
 */
public class BreadthFirstSearchJobLauncher extends MapReduceJobLauncher {

	private BFSParameters getParameters() {
    	assert (parameters instanceof BFSParameters);
    	return (BFSParameters)parameters;
    }

	@Override
	protected MapReduceJob<?> createDirectedJob(String input, String intermediate, String output) {
		return new BreadthFirstSearchJob(input, intermediate, output, getParameters(), true);
	}

	@Override
	protected MapReduceJob<?> createUndirectedJob(String input, String intermediate, String output) {
		return new BreadthFirstSearchJob(input, intermediate, output, getParameters(), false);
	}
}
