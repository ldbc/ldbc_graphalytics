/*
 * Copyright 2015 - 2017 Atlarge Research Team,
 * operating at Technische Universiteit Delft
 * and Vrije Universiteit Amsterdam, the Netherlands.
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
import science.atlarge.graphalytics.report.result.BenchmarkRunResult;

import java.util.*;

/**
 * Wrapper class for BenchmarkResult, with many convenient accessors for use by the templating engine.
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class BenchmarkReportData {

	private final BenchmarkResult benchmarkResult;
	private final Collection<Graph> orderedGraphCollection;
	private final Collection<Algorithm> orderedAlgorithmCollection;
	/**
	 * Is unmodifiable and contains unmodifiable maps
	 */
	private final Map<Graph, Map<Algorithm, BenchmarkRunResult>> graphAlgorithmResults;
	/**
	 * Is unmodifiable and contains unmodifiable maps
	 */
	private final Map<Algorithm, Map<Graph, BenchmarkRunResult>> algorithmGraphResults;

	/**
	 * @param benchmarkResult the results of running a benchmark suite, from which data is extracted
	 */
	public BenchmarkReportData(BenchmarkResult benchmarkResult) {
		this.benchmarkResult = benchmarkResult;
		this.orderedGraphCollection = constructOrderedGraphSetCollection(benchmarkResult);
		this.orderedAlgorithmCollection = constructOrderedAlgorithmCollection(benchmarkResult);
		this.graphAlgorithmResults = constructGraphAlgorithmResults(benchmarkResult);
		this.algorithmGraphResults = constructAlgorithmGraphResults(benchmarkResult);
	}

	private static Collection<Graph> constructOrderedGraphSetCollection(BenchmarkResult benchmarkResult) {
		List<Graph> graphCollection = new ArrayList<>(benchmarkResult.getBenchmark().getGraphs());
		Collections.sort(graphCollection, new Comparator<Graph>() {
			@Override
			public int compare(Graph o1, Graph o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return Collections.unmodifiableList(graphCollection);
	}

	private static Collection<Algorithm> constructOrderedAlgorithmCollection(BenchmarkResult benchmarkResult) {
		List<Algorithm> algorithmCollection = new ArrayList<>(benchmarkResult.getBenchmark().getAlgorithms());
		Collections.sort(algorithmCollection, new Comparator<Algorithm>() {
			@Override
			public int compare(Algorithm o1, Algorithm o2) {
				return o1.getAcronym().compareToIgnoreCase(o2.getAcronym());
			}
		});
		return Collections.unmodifiableList(algorithmCollection);
	}

	private static Map<Graph, Map<Algorithm, BenchmarkRunResult>> constructGraphAlgorithmResults(
			BenchmarkResult benchmarkResult) {
		// Construct a map of maps to hold the results
		Map<Graph, Map<Algorithm, BenchmarkRunResult>> graphAlgorithmResults = new HashMap<>();
		for (Graph graph : benchmarkResult.getBenchmark().getGraphs()) {
			graphAlgorithmResults.put(graph, new HashMap<Algorithm, BenchmarkRunResult>());
		}

		// Insert the results from the benchmark suite
		for (BenchmarkRunResult benchmarkRunResult : benchmarkResult.getBenchmarkRunResults()) {
			graphAlgorithmResults.get(benchmarkRunResult.getBenchmarkRun().getFormattedGraph().getGraph()).put(
					benchmarkRunResult.getBenchmarkRun().getAlgorithm(), benchmarkRunResult);
		}

		// Make the map unmodifiable
		for (Graph graph : benchmarkResult.getBenchmark().getGraphs()) {
			graphAlgorithmResults.put(graph, Collections.unmodifiableMap(graphAlgorithmResults.get(graph)));
		}
		return Collections.unmodifiableMap(graphAlgorithmResults);
	}

	private static Map<Algorithm, Map<Graph, BenchmarkRunResult>> constructAlgorithmGraphResults(
			BenchmarkResult benchmarkResult) {
		// Construct a map of maps to hold the results
		Map<Algorithm, Map<Graph, BenchmarkRunResult>> algorithmGraphResults = new HashMap<>();
		for (Algorithm algorithm : benchmarkResult.getBenchmark().getAlgorithms()) {
			algorithmGraphResults.put(algorithm, new HashMap<Graph, BenchmarkRunResult>());
		}

		// Insert the results from the benchmark suite
		for (BenchmarkRunResult benchmarkRunResult : benchmarkResult.getBenchmarkRunResults()) {
			algorithmGraphResults.get(benchmarkRunResult.getBenchmarkRun().getAlgorithm()).put(
					benchmarkRunResult.getBenchmarkRun().getFormattedGraph().getGraph(), benchmarkRunResult);
		}

		// Make the map unmodifiable
		for (Algorithm algorithm : benchmarkResult.getBenchmark().getAlgorithms()) {
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
	public BenchmarkRunResult getResult(Graph graph, Algorithm algorithm) {
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
	public Map<Algorithm, BenchmarkRunResult> getResults(FormattedGraph formattedGraph) {
		return graphAlgorithmResults.get(formattedGraph);
	}

	/**
	 * @param algorithm an algorithm from the benchmark suite
	 * @return a map containing the results for executing the specified algorithm on any graph
	 */
	public Map<Graph, BenchmarkRunResult> getResults(Algorithm algorithm) {
		return algorithmGraphResults.get(algorithm);
	}

	/**
	 * @return all benchmark results, with graph as primary key and algorithm as secondary key
	 */
	public Map<Graph, Map<Algorithm, BenchmarkRunResult>> getResultsPerGraph() {
		return graphAlgorithmResults;
	}

	/**
	 * @return all benchmark results, with algorithm as primary key and graph as secondary key
	 */
	public Map<Algorithm, Map<Graph, BenchmarkRunResult>> getResultsPerAlgorithm() {
		return algorithmGraphResults;
	}


}
