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

import nl.tudelft.graphalytics.domain.benchmark.BenchmarkExperiment;
import nl.tudelft.graphalytics.domain.benchmark.BenchmarkJob;

import java.io.Serializable;
import java.util.*;

/**
 * The Graphalytics benchmark suite; a collection of benchmarks using multiple algorithms and running on multiple
 * graphs. The exact algorithms and graphs that are part of this suite are controlled by external configuration
 * files.
 *
 * @author Tim Hegeman
 */
public class BenchmarkSuite implements Serializable {

	protected final Collection<BenchmarkExperiment> experiments;
	protected final Collection<BenchmarkJob> jobs;
	protected final Collection<Benchmark> benchmarks;
	protected final Set<Algorithm> algorithms;
	protected final Set<GraphSet> graphSets;

	public BenchmarkSuite() {

		experiments = new ArrayList<>();
		jobs = new ArrayList<>();
		benchmarks = new ArrayList<>();
		algorithms = new HashSet<>();
		graphSets = new HashSet<>();
	}

	public BenchmarkSuite(Collection<BenchmarkExperiment> experiments, Collection<BenchmarkJob> jobs,
							 Collection<Benchmark> benchmarks, Set<Algorithm> algorithms,
						   Set<GraphSet> graphSets) {
		this.experiments = experiments;
		this.jobs = jobs;
		this.benchmarks = benchmarks;
		this.algorithms = algorithms;
		this.graphSets = graphSets;
	}

	public Collection<BenchmarkExperiment> getExperiments() {
		return experiments;
	}

	public Collection<BenchmarkJob> getJobs() {
		return jobs;
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

}
