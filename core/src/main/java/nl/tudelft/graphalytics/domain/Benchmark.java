package nl.tudelft.graphalytics.domain;

import java.io.Serializable;

/**
 * A single benchmark in the Graphalytics benchmark suite. Consists of a single algorithm, a single graph,
 * and parameters for the algorithm.
 *
 * @author Tim Hegeman
 */
public class Benchmark implements Serializable {

	private final Algorithm algorithm;
	private final Graph graph;
	private final Object algorithmParameters;

	/**
	 * @param algorithm           the algorithm to run for this benchmark
	 * @param graph               the graph to run the algorithm on
	 * @param algorithmParameters parameters for the algorithm
	 */
	public Benchmark(Algorithm algorithm, Graph graph, Object algorithmParameters) {
		this.algorithm = algorithm;
		this.graph = graph;
		this.algorithmParameters = algorithmParameters;
	}

	/**
	 * @return the algorithm to run for this benchmark
	 */
	public Algorithm getAlgorithm() {
		return algorithm;
	}

	/**
	 * @return the graph to run this benchmark on
	 */
	public Graph getGraph() {
		return graph;
	}

	/**
	 * @return parameters for the algorithm
	 */
	public Object getAlgorithmParameters() {
		return algorithmParameters;
	}
}
