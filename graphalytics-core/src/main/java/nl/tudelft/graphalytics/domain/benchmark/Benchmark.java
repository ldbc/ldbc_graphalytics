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
package nl.tudelft.graphalytics.domain.benchmark;

import nl.tudelft.graphalytics.domain.algorithms.Algorithm;
import nl.tudelft.graphalytics.domain.graph.Graph;
import nl.tudelft.graphalytics.domain.graph.GraphSet;
import nl.tudelft.graphalytics.domain.graph.StandardGraph;

import java.io.Serializable;
import java.util.*;

/**
 * The Graphalytics benchmark suite; a collection of benchmarks using multiple algorithms and running on multiple
 * graphs. The exact algorithms and graphs that are part of this suite are controlled by external configuration
 * files.
 *
 * @author Tim Hegeman
 */
public class Benchmark implements Serializable {

	protected final Collection<BenchmarkExp> experiments;
	protected final Collection<BenchmarkJob> jobs;
	protected final Collection<BenchmarkRun> benchmarkRuns;
	protected final Set<Algorithm> algorithms;
	protected final Set<GraphSet> graphSets;
	protected final String outputDirectory;

	public Benchmark() {
		experiments = new ArrayList<>();
		jobs = new ArrayList<>();
		benchmarkRuns = new ArrayList<>();
		algorithms = new HashSet<>();
		graphSets = new HashSet<>();
		outputDirectory = null;
	}

	public Benchmark(Collection<BenchmarkExp> experiments, Collection<BenchmarkJob> jobs,
					 Collection<BenchmarkRun> benchmarkRuns, Set<Algorithm> algorithms,
					 Set<GraphSet> graphSets, String outputDirectory) {
		this.experiments = experiments;
		this.jobs = jobs;
		this.benchmarkRuns = benchmarkRuns;
		this.algorithms = algorithms;
		this.graphSets = graphSets;
		this.outputDirectory = outputDirectory;
	}

	public Collection<BenchmarkExp> getExperiments() {
		return experiments;
	}

	public Collection<BenchmarkJob> getJobs() {
		return jobs;
	}

	/**
	 * @return the benchmarks that make up the Graphalytics benchmark suite
	 */
	public Collection<BenchmarkRun> getBenchmarkRuns() {
		return Collections.unmodifiableCollection(benchmarkRuns);
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
	public Collection<BenchmarkRun> getBenchmarksForGraph(Graph graph) {
		Collection<BenchmarkRun> benchmarksForGraph = new ArrayList<>();
		for (BenchmarkRun benchmarkRun : benchmarkRuns) {
			if (benchmarkRun.getGraph().equals(graph)) {
				benchmarksForGraph.add(benchmarkRun);
			}
		}
		return benchmarksForGraph;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}


	protected boolean verifyGraphInfo(StandardGraph graph, GraphSet graphSet) {
		boolean eqNumEdges = graphSet.getSourceGraph().getNumberOfEdges() == graph.edgeSize;
		boolean eqNumVertices = graphSet.getSourceGraph().getNumberOfVertices() == graph.vertexSize;
		boolean eqDirectedness = graphSet.getSourceGraph().isDirected() == graph.isDirected;
		boolean eqProperties = graphSet.getSourceGraph().hasEdgeProperties() == graph.hasProperty;
		return eqNumVertices && eqNumEdges && eqDirectedness && eqProperties;
	}

}
