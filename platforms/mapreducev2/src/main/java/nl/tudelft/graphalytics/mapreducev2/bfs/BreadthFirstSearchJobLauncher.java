package nl.tudelft.graphalytics.mapreducev2.bfs;

import nl.tudelft.graphalytics.domain.algorithms.BreadthFirstSearchParameters;
import nl.tudelft.graphalytics.mapreducev2.MapReduceJobLauncher;
import nl.tudelft.graphalytics.mapreducev2.MapReduceJob;

/**
 * Job launcher for the breadth-first search algorithm on MapReduce version 2.
 *
 * @author Tim Hegeman
 */
public class BreadthFirstSearchJobLauncher extends MapReduceJobLauncher {

	private BreadthFirstSearchParameters getParameters() {
    	assert (parameters instanceof BreadthFirstSearchParameters);
    	return (BreadthFirstSearchParameters)parameters;
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
