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
package nl.tudelft.graphalytics.reporting;

import nl.tudelft.graphalytics.domain.*;

import java.util.*;

/**
 * Wrapper class for BenchmarkSuiteResult, with many convenient accessors for use by the templating engine.
 *
 * @author Tim Hegeman
 */
public class BenchmarkReportData {

	private final BenchmarkSuiteResult benchmarkSuiteResult;
	private final Collection<GraphSet> orderedGraphSetCollection;
	private final Collection<Algorithm> orderedAlgorithmCollection;
	/**
	 * Is unmodifiable and contains unmodifiable maps
	 */
	private final Map<GraphSet, Map<Algorithm, BenchmarkResult>> graphAlgorithmResults;
	/**
	 * Is unmodifiable and contains unmodifiable maps
	 */
	private final Map<Algorithm, Map<GraphSet, BenchmarkResult>> algorithmGraphResults;

	/**
	 * @param benchmarkSuiteResult the results of running a benchmark suite, from which data is extracted
	 */
	public BenchmarkReportData(BenchmarkSuiteResult benchmarkSuiteResult) {
		this.benchmarkSuiteResult = benchmarkSuiteResult;
		this.orderedGraphSetCollection = constructOrderedGraphSetCollection(benchmarkSuiteResult);
		this.orderedAlgorithmCollection = constructOrderedAlgorithmCollection(benchmarkSuiteResult);
		this.graphAlgorithmResults = constructGraphAlgorithmResults(benchmarkSuiteResult);
		this.algorithmGraphResults = constructAlgorithmGraphResults(benchmarkSuiteResult);
	}

	private static Collection<GraphSet> constructOrderedGraphSetCollection(BenchmarkSuiteResult benchmarkSuiteResult) {
		List<GraphSet> graphSetCollection = new ArrayList<>(benchmarkSuiteResult.getBenchmarkSuite().getGraphSets());
		Collections.sort(graphSetCollection, new Comparator<GraphSet>() {
			@Override
			public int compare(GraphSet o1, GraphSet o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return Collections.unmodifiableList(graphSetCollection);
	}

	private static Collection<Algorithm> constructOrderedAlgorithmCollection(BenchmarkSuiteResult benchmarkSuiteResult) {
		List<Algorithm> algorithmCollection = new ArrayList<>(benchmarkSuiteResult.getBenchmarkSuite().getAlgorithms());
		Collections.sort(algorithmCollection, new Comparator<Algorithm>() {
			@Override
			public int compare(Algorithm o1, Algorithm o2) {
				return o1.getAcronym().compareToIgnoreCase(o2.getAcronym());
			}
		});
		return Collections.unmodifiableList(algorithmCollection);
	}

	private static Map<GraphSet, Map<Algorithm, BenchmarkResult>> constructGraphAlgorithmResults(
			BenchmarkSuiteResult benchmarkSuiteResult) {
		// Construct a map of maps to hold the results
		Map<GraphSet, Map<Algorithm, BenchmarkResult>> graphAlgorithmResults = new HashMap<>();
		for (GraphSet graph : benchmarkSuiteResult.getBenchmarkSuite().getGraphSets()) {
			graphAlgorithmResults.put(graph, new HashMap<Algorithm, BenchmarkResult>());
		}

		// Insert the results from the benchmark suite
		for (BenchmarkResult benchmarkResult : benchmarkSuiteResult.getBenchmarkResults()) {
			graphAlgorithmResults.get(benchmarkResult.getBenchmark().getGraph().getGraphSet()).put(
					benchmarkResult.getBenchmark().getAlgorithm(), benchmarkResult);
		}

		// Make the map unmodifiable
		for (GraphSet graph : benchmarkSuiteResult.getBenchmarkSuite().getGraphSets()) {
			graphAlgorithmResults.put(graph, Collections.unmodifiableMap(graphAlgorithmResults.get(graph)));
		}
		return Collections.unmodifiableMap(graphAlgorithmResults);
	}

	private static Map<Algorithm, Map<GraphSet, BenchmarkResult>> constructAlgorithmGraphResults(
			BenchmarkSuiteResult benchmarkSuiteResult) {
		// Construct a map of maps to hold the results
		Map<Algorithm, Map<GraphSet, BenchmarkResult>> algorithmGraphResults = new HashMap<>();
		for (Algorithm algorithm : benchmarkSuiteResult.getBenchmarkSuite().getAlgorithms()) {
			algorithmGraphResults.put(algorithm, new HashMap<GraphSet, BenchmarkResult>());
		}

		// Insert the results from the benchmark suite
		for (BenchmarkResult benchmarkResult : benchmarkSuiteResult.getBenchmarkResults()) {
			algorithmGraphResults.get(benchmarkResult.getBenchmark().getAlgorithm()).put(
					benchmarkResult.getBenchmark().getGraph().getGraphSet(), benchmarkResult);
		}

		// Make the map unmodifiable
		for (Algorithm algorithm : benchmarkSuiteResult.getBenchmarkSuite().getAlgorithms()) {
			algorithmGraphResults.put(algorithm, Collections.unmodifiableMap(algorithmGraphResults.get(algorithm)));
		}
		return Collections.unmodifiableMap(algorithmGraphResults);
	}

	/**
	 * @return an ordered collection of all graph sets processed in the benchmark suite
	 */
	public Collection<GraphSet> getGraphSets() {
		return orderedGraphSetCollection;
	}

	/**
	 * @return an ordered collection of all algorithms executed in the benchmark suite
	 */
	public Collection<Algorithm> getAlgorithms() {
		return orderedAlgorithmCollection;
	}

	/**
	 * @param graphSet  a graph set from the benchmark suite
	 * @param algorithm an algorithm from the benchmark suite
	 * @return the execution results for executing the specified algorithm on the specified graph
	 */
	public BenchmarkResult getResult(GraphSet graphSet, Algorithm algorithm) {
		return graphAlgorithmResults.get(graphSet).get(algorithm);
	}

	/**
	 * @param graphSet a graph set from the benchmark suite
	 * @return a map containing the results for executing any algorithm on the specified graph
	 */
	public Map<Algorithm, BenchmarkResult> getResults(Graph graphSet) {
		return graphAlgorithmResults.get(graphSet);
	}

	/**
	 * @param algorithm an algorithm from the benchmark suite
	 * @return a map containing the results for executing the specified algorithm on any graph
	 */
	public Map<GraphSet, BenchmarkResult> getResults(Algorithm algorithm) {
		return algorithmGraphResults.get(algorithm);
	}

	/**
	 * @return all benchmark results, with graph as primary key and algorithm as secondary key
	 */
	public Map<GraphSet, Map<Algorithm, BenchmarkResult>> getResultsPerGraph() {
		return graphAlgorithmResults;
	}

	/**
	 * @return all benchmark results, with algorithm as primary key and graph as secondary key
	 */
	public Map<Algorithm, Map<GraphSet, BenchmarkResult>> getResultsPerAlgorithm() {
		return algorithmGraphResults;
	}

	/**
	 * @return benchmark configuration details
	 */
	public NestedConfiguration getBenchmarkConfiguration() {
		return benchmarkSuiteResult.getBenchmarkConfiguration();
	}

	/**
	 * @return platform-specific configuration details
	 */
	public NestedConfiguration getPlatformConfiguration() {
		return benchmarkSuiteResult.getPlatformConfiguration();
	}

	/**
	 * @return information about the system used to run the benchmark suite on
	 */
	public SystemDetails getSystemDetails() {
		return benchmarkSuiteResult.getSystemDetails();
	}

}
