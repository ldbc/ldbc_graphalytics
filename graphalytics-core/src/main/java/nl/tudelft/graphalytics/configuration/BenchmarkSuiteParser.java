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
package nl.tudelft.graphalytics.configuration;

import nl.tudelft.graphalytics.domain.*;
import nl.tudelft.graphalytics.domain.algorithms.AlgorithmParameters;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Helper class for loading the Graphalytics benchmark suite data from a properties file.
 *
 * @author Tim Hegeman
 */
public final class BenchmarkSuiteParser {
	private static final Logger LOG = LogManager.getLogger();

	private static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";
	private static final String BENCHMARK_RUN_GRAPHS_KEY = "benchmark.run.graphs";
	private static final String BENCHMARK_RUN_ALGORITHMS_KEY = "benchmark.run.algorithms";
	private static final String BENCHMARK_RUN_OUTPUT_REQUIRED_KEY = "benchmark.run.output-required";
	private static final String BENCHMARK_RUN_OUTPUT_DIRECTORY_KEY = "benchmark.run.output-directory";
	private static final String GRAPHS_ROOT_DIRECTORY_KEY = "graphs.root-directory";
	private static final String GRAPHS_CACHE_DIRECTORY_KEY = "graphs.cache-directory";
	private static final String GRAPHS_NAMES_KEY = "graphs.names";

	private final Configuration benchmarkConfiguration;

	// Cached properties
	private boolean outputRequired;
	private Path outputDirectory;
	private String graphRootDirectory;
	private String graphCacheDirectory;
	private Map<String, GraphSet> graphSets;
	private Map<String, Map<Algorithm, AlgorithmParameters>> algorithmParametersPerGraphSet;
	private Set<Benchmark> benchmarks;
	private Set<GraphSet> graphSelection;
	private Set<Algorithm> algorithmSelection;

	private BenchmarkSuite benchmarkSuite = null;

	private BenchmarkSuiteParser(Configuration benchmarkConfiguration) {
		this.benchmarkConfiguration = benchmarkConfiguration;
	}

	/**
	 * Parses a BenchmarkSuite object from the "benchmark.properties" file found on the classpath.
	 *
	 * @return the parsed BenchmarkSuite
	 * @throws ConfigurationException        if the "benchmark.properties" file could not be loaded
	 * @throws InvalidConfigurationException if the "benchmark.properties" files is missing properties or has invalid
	 *                                       values for properties
	 */
	public static BenchmarkSuite readBenchmarkSuiteFromProperties()
			throws ConfigurationException, InvalidConfigurationException {
		Configuration graphConfiguration = new PropertiesConfiguration(BENCHMARK_PROPERTIES_FILE);
		return new BenchmarkSuiteParser(graphConfiguration).parse();
	}

	private BenchmarkSuite parse() throws InvalidConfigurationException {
		if (benchmarkSuite != null) {
			return benchmarkSuite;
		}

		outputRequired = ConfigurationUtil.getBoolean(benchmarkConfiguration, BENCHMARK_RUN_OUTPUT_REQUIRED_KEY);
		if (outputRequired) {
			outputDirectory = Paths.get(ConfigurationUtil.getString(benchmarkConfiguration, BENCHMARK_RUN_OUTPUT_DIRECTORY_KEY));
		} else {
			outputDirectory = Paths.get(".");
		}

		graphRootDirectory = ConfigurationUtil.getString(benchmarkConfiguration, GRAPHS_ROOT_DIRECTORY_KEY);
		graphCacheDirectory = benchmarkConfiguration.getString(GRAPHS_CACHE_DIRECTORY_KEY,
				Paths.get(graphRootDirectory, "cache").toString());

		Collection<GraphSetParser> graphSetParsers = constructGraphSetParsers();
		parseGraphSetsAndAlgorithmParameters(graphSetParsers);
		constructBenchmarks();
		parseGraphSetSelection();
		parseAlgorithmSelection();

		return BenchmarkSuite.fromBenchmarks(benchmarks).getSubset(algorithmSelection, graphSelection);
	}

	private Collection<GraphSetParser> constructGraphSetParsers()
			throws InvalidConfigurationException {
		// Get list of available graph sets
		String[] graphNames = ConfigurationUtil.getStringArray(benchmarkConfiguration, GRAPHS_NAMES_KEY);

		// Parse each graph set individually
		List<GraphSetParser> parsedGraphSets = new ArrayList<>(graphNames.length);
		for (String graphName : graphNames) {
			parsedGraphSets.add(new GraphSetParser(benchmarkConfiguration.subset("graph." + graphName),
					graphName, graphRootDirectory, graphCacheDirectory));
		}

		return parsedGraphSets;
	}

	private void parseGraphSetsAndAlgorithmParameters(Collection<GraphSetParser> graphSetParsers)
			throws InvalidConfigurationException {
		graphSets = new HashMap<>();
		algorithmParametersPerGraphSet = new HashMap<>();
		for (GraphSetParser parser : graphSetParsers) {
			GraphSet graphSet = parser.parseGraphSet();
			if (!graphExists(graphSet.getSourceGraph())) {
				LOG.warn("Could not find file for graph \"" + graphSet.getName() + "\" at paths \"" +
						graphSet.getSourceGraph().getVertexFilePath() + "\" and \"" +
						graphSet.getSourceGraph().getEdgeFilePath() + "\". Skipping.");
				continue;
			}

			graphSets.put(graphSet.getName(), graphSet);
			algorithmParametersPerGraphSet.put(graphSet.getName(), parser.parseAlgorithmParameters());
		}
	}

	private boolean graphExists(Graph graph) {
		return new File(graph.getVertexFilePath()).isFile() && new File(graph.getEdgeFilePath()).isFile();
	}

	private void constructBenchmarks() throws InvalidConfigurationException {
		benchmarks = new HashSet<>();
		for (String graphName : graphSets.keySet()) {
			Map<Algorithm, AlgorithmParameters> algorithmParameters = algorithmParametersPerGraphSet.get(graphName);
			Map<Algorithm, Graph> graphPerAlgorithm = graphSets.get(graphName).getGraphPerAlgorithm();
			for (Algorithm algorithm : algorithmParameters.keySet()) {
				benchmarks.add(new Benchmark(algorithm, graphPerAlgorithm.get(algorithm),
						algorithmParameters.get(algorithm), outputRequired,
						outputDirectory.resolve(graphName + "-" + algorithm.getAcronym()).toString()));
			}
		}
	}

	private void parseGraphSetSelection() throws InvalidConfigurationException {
		// Get list of selected graphs
		String[] graphSelectionNames = benchmarkConfiguration.getStringArray(BENCHMARK_RUN_GRAPHS_KEY);

		// Set graph selection to all graphs if the selection property is empty
		if (graphSelectionNames.length == 0 || (graphSelectionNames.length == 1 && graphSelectionNames[0].isEmpty())) {
			graphSelection = new HashSet<>(graphSets.values());
			return;
		}

		// Parse the graph names
		graphSelection = new HashSet<>();
		for (String graphSelectionName : graphSelectionNames) {
			if (graphSets.containsKey(graphSelectionName)) {
				graphSelection.add(graphSets.get(graphSelectionName));
			} else if (!graphSelectionName.isEmpty()) {
				LOG.warn("Found unknown graph name \"" + graphSelectionName + "\" in property \"" +
						BENCHMARK_RUN_GRAPHS_KEY + "\".");
			} else {
				throw new InvalidConfigurationException("Incorrectly formatted selection of graph names in property \"" +
						BENCHMARK_RUN_GRAPHS_KEY + "\".");
			}
		}
	}

	private void parseAlgorithmSelection() throws InvalidConfigurationException {
		// Get list of selected algorithms
		String[] algorithmSelectionNames = benchmarkConfiguration.getStringArray(BENCHMARK_RUN_ALGORITHMS_KEY);

		// Set algorithm selection to all algorithms if the selection property is empty
		if (algorithmSelectionNames.length == 0 || (algorithmSelectionNames.length == 1 && algorithmSelectionNames[0].isEmpty())) {
			algorithmSelection = new HashSet<>(Arrays.asList(Algorithm.values()));
			return;
		}

		// Parse the algorithm acronyms
		algorithmSelection = new HashSet<>();
		for (String algorithmSelectionName : algorithmSelectionNames) {
			Algorithm algorithm = Algorithm.fromAcronym(algorithmSelectionName);
			if (algorithm != null) {
				algorithmSelection.add(algorithm);
			} else if (!algorithmSelectionName.isEmpty()) {
				LOG.warn("Found unknown algorithm name \"" + algorithmSelectionName + "\" in property \"" +
						BENCHMARK_RUN_ALGORITHMS_KEY + "\".");
			} else {
				throw new InvalidConfigurationException("Incorrectly formatted selection of algorithm names in property \"" +
						BENCHMARK_RUN_ALGORITHMS_KEY + "\".");
			}
		}
	}

}
