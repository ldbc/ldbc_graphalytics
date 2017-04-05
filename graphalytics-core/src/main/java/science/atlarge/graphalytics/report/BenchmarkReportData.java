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
package science.atlarge.graphalytics.report;

import science.atlarge.graphalytics.domain.algorithms.Algorithm;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import science.atlarge.graphalytics.report.result.BenchmarkResult;
import science.atlarge.graphalytics.report.result.BenchmarkSuiteResult;

import java.util.*;

/**
 * Wrapper class for BenchmarkSuiteResult, with many convenient accessors for use by the templating engine.
 *
 * @author Tim Hegeman
 */
public class BenchmarkReportData {

	private final BenchmarkSuiteResult benchmarkSuiteResult;
	private final Collection<Graph> orderedGraphCollection;
	private final Collection<Algorithm> orderedAlgorithmCollection;
	/**
	 * Is unmodifiable and contains unmodifiable maps
	 */
	private final Map<Graph, Map<Algorithm, BenchmarkResult>> graphAlgorithmResults;
	/**
	 * Is unmodifiable and contains unmodifiable maps
	 */
	private final Map<Algorithm, Map<Graph, BenchmarkResult>> algorithmGraphResults;

	/**
	 * @param benchmarkSuiteResult the results of running a benchmark suite, from which data is extracted
	 */
	public BenchmarkReportData(BenchmarkSuiteResult benchmarkSuiteResult) {
		this.benchmarkSuiteResult = benchmarkSuiteResult;
		this.orderedGraphCollection = constructOrderedGraphSetCollection(benchmarkSuiteResult);
		this.orderedAlgorithmCollection = constructOrderedAlgorithmCollection(benchmarkSuiteResult);
		this.graphAlgorithmResults = constructGraphAlgorithmResults(benchmarkSuiteResult);
		this.algorithmGraphResults = constructAlgorithmGraphResults(benchmarkSuiteResult);
	}

	private static Collection<Graph> constructOrderedGraphSetCollection(BenchmarkSuiteResult benchmarkSuiteResult) {
		List<Graph> graphCollection = new ArrayList<>(benchmarkSuiteResult.getBenchmark().getGraphs());
		Collections.sort(graphCollection, new Comparator<Graph>() {
			@Override
			public int compare(Graph o1, Graph o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return Collections.unmodifiableList(graphCollection);
	}

	private static Collection<Algorithm> constructOrderedAlgorithmCollection(BenchmarkSuiteResult benchmarkSuiteResult) {
		List<Algorithm> algorithmCollection = new ArrayList<>(benchmarkSuiteResult.getBenchmark().getAlgorithms());
		Collections.sort(algorithmCollection, new Comparator<Algorithm>() {
			@Override
			public int compare(Algorithm o1, Algorithm o2) {
				return o1.getAcronym().compareToIgnoreCase(o2.getAcronym());
			}
		});
		return Collections.unmodifiableList(algorithmCollection);
	}

	private static Map<Graph, Map<Algorithm, BenchmarkResult>> constructGraphAlgorithmResults(
			BenchmarkSuiteResult benchmarkSuiteResult) {
		// Construct a map of maps to hold the results
		Map<Graph, Map<Algorithm, BenchmarkResult>> graphAlgorithmResults = new HashMap<>();
		for (Graph graph : benchmarkSuiteResult.getBenchmark().getGraphs()) {
			graphAlgorithmResults.put(graph, new HashMap<Algorithm, BenchmarkResult>());
		}

		// Insert the results from the benchmark suite
		for (BenchmarkResult benchmarkResult : benchmarkSuiteResult.getBenchmarkResults()) {
			graphAlgorithmResults.get(benchmarkResult.getBenchmarkRun().getFormattedGraph().getGraph()).put(
					benchmarkResult.getBenchmarkRun().getAlgorithm(), benchmarkResult);
		}

		// Make the map unmodifiable
		for (Graph graph : benchmarkSuiteResult.getBenchmark().getGraphs()) {
			graphAlgorithmResults.put(graph, Collections.unmodifiableMap(graphAlgorithmResults.get(graph)));
		}
		return Collections.unmodifiableMap(graphAlgorithmResults);
	}

	private static Map<Algorithm, Map<Graph, BenchmarkResult>> constructAlgorithmGraphResults(
			BenchmarkSuiteResult benchmarkSuiteResult) {
		// Construct a map of maps to hold the results
		Map<Algorithm, Map<Graph, BenchmarkResult>> algorithmGraphResults = new HashMap<>();
		for (Algorithm algorithm : benchmarkSuiteResult.getBenchmark().getAlgorithms()) {
			algorithmGraphResults.put(algorithm, new HashMap<Graph, BenchmarkResult>());
		}

		// Insert the results from the benchmark suite
		for (BenchmarkResult benchmarkResult : benchmarkSuiteResult.getBenchmarkResults()) {
			algorithmGraphResults.get(benchmarkResult.getBenchmarkRun().getAlgorithm()).put(
					benchmarkResult.getBenchmarkRun().getFormattedGraph().getGraph(), benchmarkResult);
		}

		// Make the map unmodifiable
		for (Algorithm algorithm : benchmarkSuiteResult.getBenchmark().getAlgorithms()) {
			algorithmGraphResults.put(algorithm, Collections.unmodifiableMap(algorithmGraphResults.get(algorithm)));
		}
		return Collections.unmodifiableMap(algorithmGraphResults);
	}

	/**
	 * @return an ordered collection of all graph sets processed in the benchmark suite
	 */
	public Collection<Graph> getGraphSets() {
		return orderedGraphCollection;
	}

	/**
	 * @return an ordered collection of all algorithms executed in the benchmark suite
	 */
	public Collection<Algorithm> getAlgorithms() {
		return orderedAlgorithmCollection;
	}

	/**
	 * @param graph  a graph set from the benchmark suite
	 * @param algorithm an algorithm from the benchmark suite
	 * @return the execution results for executing the specified algorithm on the specified graph
	 */
	public BenchmarkResult getResult(Graph graph, Algorithm algorithm) {
		return graphAlgorithmResults.get(graph).get(algorithm);
	}

	/**
	 * @param graph  a graph set from the benchmark suite
	 * @param algorithm an algorithm from the benchmark suite
	 * @return true iff a benchmark result exists for the given pair of graph and algorithm
	 */
	public boolean wasExecuted(Graph graph, Algorithm algorithm) {
		return getResult(graph, algorithm) != null;
	}

	/**
	 * @param formattedGraph a graph set from the benchmark suite
	 * @return a map containing the results for executing any algorithm on the specified graph
	 */
	public Map<Algorithm, BenchmarkResult> getResults(FormattedGraph formattedGraph) {
		return graphAlgorithmResults.get(formattedGraph);
	}

	/**
	 * @param algorithm an algorithm from the benchmark suite
	 * @return a map containing the results for executing the specified algorithm on any graph
	 */
	public Map<Graph, BenchmarkResult> getResults(Algorithm algorithm) {
		return algorithmGraphResults.get(algorithm);
	}

	/**
	 * @return all benchmark results, with graph as primary key and algorithm as secondary key
	 */
	public Map<Graph, Map<Algorithm, BenchmarkResult>> getResultsPerGraph() {
		return graphAlgorithmResults;
	}

	/**
	 * @return all benchmark results, with algorithm as primary key and graph as secondary key
	 */
	public Map<Algorithm, Map<Graph, BenchmarkResult>> getResultsPerAlgorithm() {
		return algorithmGraphResults;
	}


}
