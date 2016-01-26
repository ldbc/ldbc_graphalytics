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
package nl.tudelft.graphalytics;

import nl.tudelft.graphalytics.configuration.ConfigurationUtil;
import nl.tudelft.graphalytics.configuration.GraphPropertiesParser;
import nl.tudelft.graphalytics.configuration.InvalidConfigurationException;
import nl.tudelft.graphalytics.domain.*;
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
public final class BenchmarkSuiteLoader {
	private static final Logger LOG = LogManager.getLogger();

	private static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";
	private static final String BENCHMARK_RUN_GRAPHS_KEY = "benchmark.run.graphs";
	private static final String BENCHMARK_RUN_ALGORITHMS_KEY = "benchmark.run.algorithms";
	private static final String BENCHMARK_RUN_OUTPUT_REQUIRED_KEY = "benchmark.run.output-required";
	private static final String BENCHMARK_RUN_OUTPUT_DIRECTORY_KEY = "benchmark.run.output-directory";
	private static final String GRAPHS_ROOT_DIRECTORY_KEY = "graphs.root-directory";
	private static final String GRAPHS_NAMES_KEY = "graphs.names";

	private Configuration benchmarkConfiguration;
	// Cached properties
	private boolean outputRequired;
	private Path outputDirectory;

	private BenchmarkSuiteLoader(Configuration benchmarkConfiguration) {
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
		return new BenchmarkSuiteLoader(graphConfiguration).parse();
	}

	private BenchmarkSuite parse() throws InvalidConfigurationException {
		outputRequired = ConfigurationUtil.getBoolean(benchmarkConfiguration, BENCHMARK_RUN_OUTPUT_REQUIRED_KEY);
		if (outputRequired) {
			outputDirectory = Paths.get(ConfigurationUtil.getString(benchmarkConfiguration, BENCHMARK_RUN_OUTPUT_DIRECTORY_KEY));
		} else {
			outputDirectory = Paths.get(".");
		}

		String rootDirectory = ConfigurationUtil.getString(benchmarkConfiguration, GRAPHS_ROOT_DIRECTORY_KEY);
		Map<String, Graph> graphs = parseGraphs(rootDirectory);
		Set<Benchmark> benchmarks = parseBenchmarks(graphs);
		Set<Graph> graphSelection = parseGraphSelection(graphs);
		Set<Algorithm> algorithmSelection = parseAlgorithmSelection();

		return BenchmarkSuite.fromBenchmarks(benchmarks).getSubset(algorithmSelection, graphSelection);
	}

	private Map<String, Graph> parseGraphs(String rootDirectory) throws InvalidConfigurationException {
		Map<String, Graph> graphs = new HashMap<>();

		// Get list of available graphs
		String[] graphNames = ConfigurationUtil.getStringArray(benchmarkConfiguration, GRAPHS_NAMES_KEY);

		// Parse each graph individually
		for (String graphName : graphNames) {
			Graph graph = new GraphPropertiesParser(benchmarkConfiguration.subset("graph." + graphName),
					graphName, rootDirectory).parseGraph();
			if (graphExists(graph)) {
				graphs.put(graphName, graph);
			} else {
				LOG.warn("Could not find file for graph \"" + graphName + "\" at paths \"" + graph.getVertexFilePath() +
						"\" and \"" + graph.getEdgeFilePath() + "\". Skipping.");
			}
		}
		return graphs;
	}

	private boolean graphExists(Graph graph) {
		return new File(graph.getVertexFilePath()).isFile() && new File(graph.getEdgeFilePath()).isFile();
	}

	private Set<Benchmark> parseBenchmarks(Map<String, Graph> graphs) throws InvalidConfigurationException {
		Set<Benchmark> benchmarks = new HashSet<>();

		// For each graph: parse per-algorithm parameters
		for (Map.Entry<String, Graph> graphEntry : graphs.entrySet()) {
			benchmarks.addAll(parseBenchmarksForGraph(graphEntry.getValue()));
		}

		return benchmarks;
	}

	private Set<Benchmark> parseBenchmarksForGraph(Graph graph) throws InvalidConfigurationException {
		Set<Benchmark> benchmarks = new HashSet<>();

		// Get list of supported algorithms
		String graphAlgorithmsKey = "graph." + graph.getName() + ".algorithms";
		String[] algorithmNames = ConfigurationUtil.getStringArray(benchmarkConfiguration, graphAlgorithmsKey);
		for (String algorithmName : algorithmNames) {
			Algorithm algorithm = Algorithm.fromAcronym(algorithmName);
			if (algorithm != null) {
				Object parameters = algorithm.getParameterFactory().fromConfiguration(
						benchmarkConfiguration, "graph." + graph.getName() + "." + algorithm.getAcronym().toLowerCase());
				benchmarks.add(new Benchmark(algorithm, graph, parameters, outputRequired,
						outputDirectory.resolve(graph.getName() + "-" + algorithm.getAcronym()).toString()));
			} else {
				LOG.warn("Found unknown algorithm name \"" + algorithmName + "\" in property \"" +
						graphAlgorithmsKey + "\".");
			}
		}

		return benchmarks;
	}

	private Set<Graph> parseGraphSelection(Map<String, Graph> graphs) {
		Set<Graph> graphSelection = new HashSet<>();

		// Get list of selected graphs
		String[] graphSelectionNames = benchmarkConfiguration.getStringArray(BENCHMARK_RUN_GRAPHS_KEY);

		// Parse the graph names
		for (String graphSelectionName : graphSelectionNames) {
			if (graphs.containsKey(graphSelectionName)) {
				graphSelection.add(graphs.get(graphSelectionName));
			} else if (!graphSelectionName.isEmpty()) {
				LOG.warn("Found unknown graph name \"" + graphSelectionName + "\" in property \"" +
						BENCHMARK_RUN_GRAPHS_KEY + "\".");
			}
		}

		// Return null if empty to select all graphs, otherwise return the set
		if (graphSelection.isEmpty()) {
			return null;
		}
		return graphSelection;
	}

	private Set<Algorithm> parseAlgorithmSelection() {
		Set<Algorithm> algorithmSelection = new HashSet<>();

		// Get list of selected algorithms
		String[] algorithmSelectionNames = benchmarkConfiguration.getStringArray(BENCHMARK_RUN_ALGORITHMS_KEY);

		// Parse the algorithm acronyms
		for (String algorithmSelectionName : algorithmSelectionNames) {
			Algorithm algorithm = Algorithm.fromAcronym(algorithmSelectionName);
			if (algorithm != null) {
				algorithmSelection.add(algorithm);
			} else if (!algorithmSelectionName.isEmpty()) {
				LOG.warn("Found unknown algorithm name \"" + algorithmSelectionName + "\" in property \"" +
						BENCHMARK_RUN_ALGORITHMS_KEY + "\".");
			}
		}

		// Return null if empty to select all algorithms, otherwise return the set
		if (algorithmSelection.isEmpty()) {
			return null;
		}
		return algorithmSelection;
	}

}
