/*
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	private final Set<GraphSet> graphSets;

	private BenchmarkSuite(Collection<Benchmark> benchmarks, Set<Algorithm> algorithms,
	                       Set<GraphSet> graphSets) {
		this.benchmarks = benchmarks;
		this.algorithms = algorithms;
		this.graphSets = graphSets;
	}

	/**
	 * @param benchmarks a collection of benchmarks that are part of the Graphalytics benchmark suite
	 * @return a BenchmarkSuite object based on the given collection of benchmarks
	 */
	public static BenchmarkSuite fromBenchmarks(Collection<Benchmark> benchmarks) {
		Set<Algorithm> algorithmSet = new HashSet<>();
		Set<GraphSet> graphSets = new HashSet<>();

		for (Benchmark benchmark : benchmarks) {
			algorithmSet.add(benchmark.getAlgorithm());
			graphSets.add(benchmark.getGraph().getGraphSet());
		}

		return new BenchmarkSuite(new ArrayList<>(benchmarks), algorithmSet, graphSets);
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
	public Set<GraphSet> getGraphSets() {
		return Collections.unmodifiableSet(graphSets);
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
	 * @param algorithms the subset of algorithms to select
	 * @param graphSets  the subset of graph sets to select
	 * @return a BenchmarkSuite with the specified subset of benchmarks
	 */
	public BenchmarkSuite getSubset(Set<Algorithm> algorithms, Set<GraphSet> graphSets) {
		Collection<Benchmark> benchmarks = new ArrayList<>();
		for (Benchmark benchmark : this.benchmarks) {
			if (algorithms.contains(benchmark.getAlgorithm()) && graphSets.contains(benchmark.getGraph().getGraphSet()))
				benchmarks.add(benchmark);
		}
		return new BenchmarkSuite(benchmarks, new HashSet<>(algorithms), new HashSet<>(graphSets));
	}

}
