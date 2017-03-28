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
package nl.tudelft.graphalytics.execution;

import nl.tudelft.graphalytics.configuration.ConfigurationUtil;
import nl.tudelft.graphalytics.configuration.GraphSetParser;
import nl.tudelft.graphalytics.configuration.InvalidConfigurationException;
import nl.tudelft.graphalytics.domain.algorithms.Algorithm;
import nl.tudelft.graphalytics.domain.algorithms.AlgorithmParameters;
import nl.tudelft.graphalytics.domain.benchmark.*;
import nl.tudelft.graphalytics.domain.graph.Graph;
import nl.tudelft.graphalytics.domain.graph.GraphSet;
import nl.tudelft.graphalytics.report.BenchmarkReportWriter;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Helper class for loading the Graphalytics benchmark data from a properties file.
 *
 * @author Tim Hegeman
 */
public final class BenchmarkLoader {
	private static final Logger LOG = LogManager.getLogger();

	private static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";
	private static final String BENCHMARK_RUN_NAME = "benchmark.name";
	private static final String BENCHMARK_RUN_TYPE = "benchmark.type";
	private static final String BENCHMARK_RUN_TARGET_SCALE = "benchmark.target-scale";
	private static final String BENCHMARK_RUN_GRAPHS_KEY = "benchmark.run.graphs";
	private static final String BENCHMARK_RUN_ALGORITHMS_KEY = "benchmark.run.algorithms";
	private static final String BENCHMARK_RUN_OUTPUT_REQUIRED_KEY = "benchmark.run.output-required";
	private static final String BENCHMARK_RUN_OUTPUT_DIRECTORY_KEY = "benchmark.run.output-directory";
	private static final String BENCHMARK_RUN_VALIDATION_REQUIRED_KEY = "benchmark.run.validation-required";
	private static final String GRAPHS_VALIDATION_DIRECTORY_KEY = "benchmark.run.validation-directory";
	private static final String GRAPHS_ROOT_DIRECTORY_KEY = "graphs.root-directory";
	private static final String GRAPHS_CACHE_DIRECTORY_KEY = "graphs.cache-directory";
	private static final String GRAPHS_NAMES_KEY = "graphs.names";

	private final Configuration benchmarkConfiguration;

	// Cached properties
	private boolean validationRequired;
	private boolean outputRequired;
	private Path outputDirectory;
	private String graphRootDirectory;
	private String graphCacheDirectory;
	private Path validationDirectory;
	private Map<String, GraphSet> graphSets;
	private Map<String, Map<Algorithm, AlgorithmParameters>> algorithmParametersPerGraphSet;

	private Benchmark benchmark = null;

	String platformName;

	public BenchmarkLoader(String platformName) {

		Configuration graphConfiguration = null;
		try {
			graphConfiguration = new PropertiesConfiguration(BENCHMARK_PROPERTIES_FILE);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		this.benchmarkConfiguration = graphConfiguration;
		this.platformName = platformName;
	}


	public Benchmark parse() throws InvalidConfigurationException {
		if (this.benchmark != null) {
			return this.benchmark;
		}

		outputRequired = ConfigurationUtil.getBoolean(benchmarkConfiguration, BENCHMARK_RUN_OUTPUT_REQUIRED_KEY);
		if (outputRequired) {
			outputDirectory = Paths.get(ConfigurationUtil.getString(benchmarkConfiguration, BENCHMARK_RUN_OUTPUT_DIRECTORY_KEY));
		} else {
			outputDirectory = Paths.get(".");
		}

		validationRequired = ConfigurationUtil.getBoolean(benchmarkConfiguration, BENCHMARK_RUN_VALIDATION_REQUIRED_KEY);

		if (validationRequired && !outputRequired) {
			LOG.warn("Validation can only be enabled if output is generated. "
					+ "Please enable the key " + BENCHMARK_RUN_OUTPUT_REQUIRED_KEY + " in your configuration.");
			LOG.info("Validation will be disabled for all benchmarks.");
			validationRequired = false;
		}

		graphRootDirectory = ConfigurationUtil.getString(benchmarkConfiguration, GRAPHS_ROOT_DIRECTORY_KEY);
		graphCacheDirectory = benchmarkConfiguration.getString(GRAPHS_CACHE_DIRECTORY_KEY,
				Paths.get(graphRootDirectory, "cache").toString());
		validationDirectory = Paths.get(benchmarkConfiguration.getString(GRAPHS_VALIDATION_DIRECTORY_KEY,
				graphRootDirectory));

		Collection<GraphSetParser> graphSetParsers = constructGraphSetParsers();
		parseGraphSetsAndAlgorithmParameters(graphSetParsers);


		String benchmarkName = benchmarkConfiguration.getString(BENCHMARK_RUN_NAME);
		String benchmarkType = benchmarkConfiguration.getString(BENCHMARK_RUN_TYPE);
		String targetScale = benchmarkConfiguration.getString(BENCHMARK_RUN_TARGET_SCALE);
		Benchmark benchmark;
		switch (benchmarkType) {
			case "test":
				LOG.info(String.format("Executing a test benchmark: \"%s (%s)\".", benchmarkName, benchmarkType));
				benchmark = constructTestBenchmarks(platformName, benchmarkType);
				break;
			case "standard":
				LOG.info(String.format("Executing a standard benchmark: \"%s (%s), Target-scale: %s\".", benchmarkName, benchmarkType, targetScale));
				benchmark = constructStandardBenchmarks(platformName, benchmarkType, targetScale);
				break;
			case "custom":
				LOG.info(String.format("Executing a customized benchmark: \"%s (%s)\".", benchmarkName, benchmarkType));
				benchmark = constructCustomBenchmarks(platformName, benchmarkType);
				break;
			default:
				throw new IllegalArgumentException("Unkown benchmark type: " + benchmarkType + ".");
		}

		return benchmark;
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
		List<String> foundGraphs = new ArrayList<>();
		List<String> lostGraphs = new ArrayList<>();
		for (GraphSetParser parser : graphSetParsers) {
			GraphSet graphSet = parser.parseGraphSet();
			if (!graphExists(graphSet.getSourceGraph())) {
				lostGraphs.add(graphSet.getName());
				LOG.trace("Could not find file for graph \"" + graphSet.getName() + "\" at paths \"" +
						graphSet.getSourceGraph().getVertexFilePath() + "\" and \"" +
						graphSet.getSourceGraph().getEdgeFilePath() + "\". Skipping.");
				continue;
			}
			foundGraphs.add(graphSet.getName());
			graphSets.put(graphSet.getName(), graphSet);
			algorithmParametersPerGraphSet.put(graphSet.getName(), parser.parseAlgorithmParameters());
		}
		LOG.info(String.format("Imported %s graph(s): %s.", foundGraphs.size(), foundGraphs));
		if(lostGraphs.size() > 0) {
			LOG.info(String.format("Failed to import %s graph(s): %s.", lostGraphs.size(), lostGraphs));
		}
	}

	private boolean graphExists(Graph graph) {
		return new File(graph.getVertexFilePath()).isFile() && new File(graph.getEdgeFilePath()).isFile();
	}

	private BenchmarkRun contructBenchmark(Algorithm algorithm, GraphSet graphSet, String outputDir) throws InvalidConfigurationException {
		if (graphSet == null) {
			LOG.error(String.format("Required graphset not available. Note that error should be caught ealier."));
			throw new IllegalStateException("Standard Benchmark: Baseline cannot be constructed due to missing graphs.");
		}

		String graphName = graphSet.getName();

		Map<Algorithm, AlgorithmParameters> algorithmParameters = algorithmParametersPerGraphSet.get(graphName);
		Map<Algorithm, Graph> graphPerAlgorithm = graphSets.get(graphName).getGraphPerAlgorithm();
                if (graphPerAlgorithm.get(algorithm) == null) {
			throw new InvalidConfigurationException(String.format(
                                    "Benchmark includes algorithm %s, which is not configured for graph %s.",
                                    algorithm.getAcronym(), graphName));
                }

		String graphAlgorithmKey = graphName + "-" + algorithm.getAcronym();

		LOG.trace(String.format("Benchmark %s-%s-%s-%s", algorithm.getAcronym(), graphPerAlgorithm.get(algorithm).getName(),
				outputDirectory.resolve(graphAlgorithmKey), validationDirectory.resolve(graphAlgorithmKey)));

		Path logPath = Paths.get(outputDir);


		return new BenchmarkRun(algorithm, graphPerAlgorithm.get(algorithm),
				algorithmParameters.get(algorithm), outputRequired,
				outputDirectory.toString(),
				validationRequired, validationDirectory.resolve(graphAlgorithmKey).toString(), logPath);
	}

	private Benchmark constructTestBenchmarks(String platformName, String benchmarkType) throws InvalidConfigurationException {
		Set<BenchmarkRun> benchmarkRuns = new HashSet<>();

		TestBenchmark baselineBenchmark = new TestBenchmark(graphSets);
		baselineBenchmark.setup();

		String outputDir = formatOuptutDirectory(platformName, benchmarkType);

		for (BenchmarkJob benchmarkJob : baselineBenchmark.getJobs()) {
			for (int i = 0; i < benchmarkJob.getRepetition(); i++) {
				BenchmarkRun benchmarkRun = contructBenchmark(benchmarkJob.getAlgorithm(), benchmarkJob.getGraphSet(), outputDir);
				benchmarkJob.addBenchmark(benchmarkRun);
				benchmarkRuns.add(benchmarkRun);
			}
		}

		Set<Algorithm> algorithmSet = new HashSet<>();
		Set<GraphSet> graphSets = new HashSet<>();

		for (BenchmarkRun benchmarkRun : benchmarkRuns) {
			algorithmSet.add(benchmarkRun.getAlgorithm());
			graphSets.add(benchmarkRun.getGraph().getGraphSet());
		}

		Benchmark benchmark = new Benchmark(
				baselineBenchmark.getExperiments(),
				baselineBenchmark.getJobs(),
				benchmarkRuns, algorithmSet, graphSets, outputDir);
		return benchmark;

	}

	private Benchmark constructStandardBenchmarks(String platformName, String benchmarkType, String targetScale) throws InvalidConfigurationException {
		Set<BenchmarkRun> benchmarkRuns = new HashSet<>();

		StandardBenchmark baselineBenchmark = new StandardBenchmark(targetScale, graphSets);
		baselineBenchmark.setup();

		String outputDir = formatOuptutDirectory(platformName, benchmarkType +"_" + targetScale);

		for (BenchmarkJob benchmarkJob : baselineBenchmark.getJobs()) {
			for (int i = 0; i < benchmarkJob.getRepetition(); i++) {
				BenchmarkRun benchmarkRun = contructBenchmark(benchmarkJob.getAlgorithm(), benchmarkJob.getGraphSet(), outputDir);
				benchmarkJob.addBenchmark(benchmarkRun);
				benchmarkRuns.add(benchmarkRun);
			}
		}

		Set<Algorithm> algorithmSet = new HashSet<>();
		Set<GraphSet> graphSets = new HashSet<>();

		for (BenchmarkRun benchmarkRun : benchmarkRuns) {
			algorithmSet.add(benchmarkRun.getAlgorithm());
			graphSets.add(benchmarkRun.getGraph().getGraphSet());
		}

		Benchmark benchmark = new Benchmark(
				baselineBenchmark.getExperiments(),
				baselineBenchmark.getJobs(),
				benchmarkRuns, algorithmSet, graphSets, outputDir);
		return benchmark;

	}


	private Benchmark constructCustomBenchmarks(String platformName, String benchmarkType) throws InvalidConfigurationException {

		List<BenchmarkExp> experiments = new ArrayList<>();
		List<BenchmarkJob> jobs = new ArrayList<>();

		BenchmarkExp experiment = new BenchmarkExp("custom");
		experiments.add(experiment);


		String[] algorithmSelectionNames = benchmarkConfiguration.getStringArray(BENCHMARK_RUN_ALGORITHMS_KEY);
		Set<Algorithm> algorithmSelection = parseAlgorithmSelection(algorithmSelectionNames);

		String[] graphSelectionNames = benchmarkConfiguration.getStringArray(BENCHMARK_RUN_GRAPHS_KEY);
		Set<GraphSet> graphSelection = parseGraphSetSelection(graphSelectionNames);

		String outputDir = formatOuptutDirectory(platformName, benchmarkType);

		Set<BenchmarkRun> benchmarkRuns = new HashSet<>();
		for (Algorithm algorithm : algorithmSelection) {
			for (GraphSet graphSet : graphSelection) {
				BenchmarkJob job = new BenchmarkJob(algorithm, graphSet, 1, 1);
				BenchmarkRun benchmarkRun = contructBenchmark(algorithm, graphSet, outputDir);
				job.addBenchmark(benchmarkRun);
				benchmarkRuns.add(benchmarkRun);
				jobs.add(job);
				experiment.addJob(job);
			}
		}

		Benchmark benchmark = new Benchmark(experiments,jobs, benchmarkRuns, algorithmSelection, graphSelection, outputDir);
		return benchmark;
	}

	private Set<GraphSet> parseGraphSetSelection(String[] graphSelectionNames) throws InvalidConfigurationException {
		Set<GraphSet> graphSelection;

		// Parse the graph names
		graphSelection = new HashSet<>();
		for (String graphSelectionName : graphSelectionNames) {
			if (graphSets.containsKey(graphSelectionName)) {
				graphSelection.add(graphSets.get(graphSelectionName));
			} else if (!graphSelectionName.isEmpty()) {
				LOG.warn("Found unknown graph name \"" + graphSelectionName + "\" in property \"" +
						BENCHMARK_RUN_GRAPHS_KEY + "\". " + " This graph may not be imported correctly due to misconfiguration.");
			} else {
				throw new InvalidConfigurationException("Incorrectly formatted selection of graph names in property \"" +
						BENCHMARK_RUN_GRAPHS_KEY + "\".");
			}
		}
		return graphSelection;
	}

	private Set<Algorithm> parseAlgorithmSelection(String[] algorithmSelectionNames) throws InvalidConfigurationException {

		Set<Algorithm> algorithmSelection;

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
		return  algorithmSelection;
	}

	private String formatOuptutDirectory(String platformName, String benchmarkType) {
		String timestamp = new SimpleDateFormat("yyMMdd-HHmmss").format(Calendar.getInstance().getTime());
		String outputDirectoryPath = String.format("report/" + "%s-%s-report-%s",
				timestamp, platformName.toUpperCase(), benchmarkType.toUpperCase());

		if(Files.exists(Paths.get(outputDirectoryPath))) {
			throw new IllegalStateException(
					String.format("Benchmark aborted: existing benchmark report detected at %s.", outputDirectoryPath));
		}
		return outputDirectoryPath;
	}



}
