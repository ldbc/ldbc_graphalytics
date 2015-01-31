package nl.tudelft.graphalytics.domain;

import java.io.Serializable;
import java.util.*;

/**
 * The Graphalytics benchmark suite; a collection of benchmarks using multiple algorithms and running on multiple
 * graphs. The exact algorithms and graphs that are part of this suite are controlled by external configuration
 * files.
 *
 * @author Tim Hegeman
 */
public final class BenchmarkSuite implements Serializable {

	private final Collection<Benchmark> benchmarks;
	private final Set<Algorithm> algorithms;
	private final Set<Graph> graphs;

	private BenchmarkSuite(Collection<Benchmark> benchmarks, Set<Algorithm> algorithms,
	                       Set<Graph> graphs) {
		this.benchmarks = benchmarks;
		this.algorithms = algorithms;
		this.graphs = graphs;
	}

	/**
	 * @param benchmarks a collection of benchmarks that are part of the Graphalytics benchmark suite
	 * @return a BenchmarkSuite object based on the given collection of benchmarks
	 */
	public static BenchmarkSuite fromBenchmarks(Collection<Benchmark> benchmarks) {
		Set<Algorithm> algorithmSet = new HashSet<>();
		Set<Graph> graphSet = new HashSet<>();

		for (Benchmark benchmark : benchmarks) {
			algorithmSet.add(benchmark.getAlgorithm());
			graphSet.add(benchmark.getGraph());
		}

		return new BenchmarkSuite(new ArrayList<>(benchmarks), algorithmSet, graphSet);
	}

	/**
	 * @return the benchmarks that make up the Graphalytics benchmark suite
	 */
	public Collection<Benchmark> getBenchmarks() {
		return Collections.unmodifiableCollection(benchmarks);
	}

	/**
	 * @return the set of algorithms used in the Graphalytics benchmark suite
	 */
	public Set<Algorithm> getAlgorithms() {
		return Collections.unmodifiableSet(algorithms);
	}

	/**
	 * @return the set of graphs used in the Graphalytics benchmark suite
	 */
	public Set<Graph> getGraphs() {
		return Collections.unmodifiableSet(graphs);
	}

	/**
	 * @param graph the graph for which to retrieve all benchmarks
	 * @return the subset of benchmarks running on the specified graph
	 */
	public Collection<Benchmark> getBenchmarksForGraph(Graph graph) {
		Collection<Benchmark> benchmarksForGraph = new ArrayList<>();
		for (Benchmark benchmark : benchmarks) {
			if (benchmark.getGraph().equals(graph)) {
				benchmarksForGraph.add(benchmark);
			}
		}
		return benchmarksForGraph;
	}

	/**
	 * Retrieves a subset of the Graphalytics benchmark suite, by keeping only benchmarks corresponding to a set of
	 * algorithms and graphs.
	 *
	 * @param algorithms the subset of algorithms to select, or null to select everything
	 * @param graphs     the subset of graphs to select, or null to select everything
	 * @return a BenchmarkSuite with the specified subset of benchmarks
	 */
	public BenchmarkSuite getSubset(Set<Algorithm> algorithms, Set<Graph> graphs) {
		if (algorithms == null)
			algorithms = this.algorithms;
		if (graphs == null)
			graphs = this.graphs;

		Collection<Benchmark> benchmarks = new ArrayList<>();
		for (Benchmark benchmark : this.benchmarks) {
			if (algorithms.contains(benchmark.getAlgorithm()) && graphs.contains(benchmark.getGraph()))
				benchmarks.add(benchmark);
		}
		return new BenchmarkSuite(benchmarks, new HashSet<>(algorithms), new HashSet<>(graphs));
	}

}
