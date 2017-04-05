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
package science.atlarge.graphalytics.execution;

import science.atlarge.graphalytics.configuration.ConfigurationUtil;
import science.atlarge.graphalytics.configuration.GraphSetParser;
import science.atlarge.graphalytics.configuration.InvalidConfigurationException;
import science.atlarge.graphalytics.domain.algorithms.Algorithm;
import science.atlarge.graphalytics.domain.algorithms.AlgorithmParameters;
import science.atlarge.graphalytics.domain.benchmark.CustomBenchmark;
import science.atlarge.graphalytics.domain.benchmark.TestBenchmark;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import science.atlarge.graphalytics.util.LogUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.domain.benchmark.Benchmark;
import science.atlarge.graphalytics.domain.benchmark.StandardBenchmark;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Helper class for loading the Graphalytics benchmark data from properties files.
 *
 * @author Tim Hegeman
 */
public final class BenchmarkLoader {
	private static final Logger LOG = LogManager.getLogger();

	private static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";
	private static final String BENCHMARK_RUN_NAME = "benchmark.name";
	private static final String BENCHMARK_RUN_TYPE = "benchmark.type";
	private static final String BENCHMARK_RUN_TARGET_SCALE = "benchmark.target-scale";
	private static final String BENCHMARK_RUN_OUTPUT_REQUIRED_KEY = "benchmark.run.output-required";
	private static final String BENCHMARK_RUN_OUTPUT_DIRECTORY_KEY = "benchmark.run.output-directory";

	private static final String BENCHMARK_RUN_TIMEOUT_KEY = "benchmark.run.timeout";
	private static final String BENCHMARK_RUN_VALIDATION_REQUIRED_KEY = "benchmark.run.validation-required";
	private static final String GRAPHS_VALIDATION_DIRECTORY_KEY = "benchmark.run.validation-directory";
	private static final String GRAPHS_ROOT_DIRECTORY_KEY = "graphs.root-directory";
	private static final String GRAPHS_CACHE_DIRECTORY_KEY = "graphs.cache-directory";
	private static final String GRAPHS_NAMES_KEY = "graphs.names";

	private final Configuration benchmarkConfiguration;

	private int timeout;
	private boolean validationRequired;
	private boolean outputRequired;
	private Path outputDirectory;
	private String baseGraphDir;
	private String baseGraphCacheDir;
	private Path baseValidationDir;
	private Map<String, Graph> foundGraphs;
	private Map<String, Map<Algorithm, AlgorithmParameters>> algorithmParameters;

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

		timeout = ConfigurationUtil.getInteger(benchmarkConfiguration, BENCHMARK_RUN_TIMEOUT_KEY);

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

		baseGraphDir = ConfigurationUtil.getString(benchmarkConfiguration, GRAPHS_ROOT_DIRECTORY_KEY);
		baseGraphCacheDir = benchmarkConfiguration.getString(GRAPHS_CACHE_DIRECTORY_KEY,
				Paths.get(baseGraphDir, "cache").toString());
		baseValidationDir = Paths.get(benchmarkConfiguration.getString(GRAPHS_VALIDATION_DIRECTORY_KEY,
				baseGraphDir));

		Collection<GraphSetParser> graphSetParsers = constructGraphSetParsers();
		parseGraphSetsAndAlgorithmParameters(graphSetParsers);


		String benchmarkType = benchmarkConfiguration.getString(BENCHMARK_RUN_TYPE);
		String targetScale = benchmarkConfiguration.getString(BENCHMARK_RUN_TARGET_SCALE);
		Benchmark benchmark;
		switch (benchmarkType) {
			case "test":
				benchmark = new TestBenchmark(benchmarkType, platformName,
						timeout, outputRequired, validationRequired,
						Paths.get("report/"), outputDirectory, baseValidationDir,
						foundGraphs, algorithmParameters);
				((TestBenchmark) benchmark).setup();
				break;

			case "standard":
				benchmark = new StandardBenchmark(benchmarkType, targetScale, platformName,
						timeout, outputRequired, validationRequired,
						Paths.get("report/"), outputDirectory, baseValidationDir,
						foundGraphs, algorithmParameters);
				((StandardBenchmark) benchmark).setup();
				break;

			case "custom":
				benchmark = new CustomBenchmark(benchmarkType, platformName,
						timeout, outputRequired, validationRequired,
						Paths.get("report/"), outputDirectory, baseValidationDir,
						foundGraphs, algorithmParameters);

				((CustomBenchmark) benchmark).setup();
				break;

			default:
				throw new IllegalArgumentException("Unkown benchmark type: " + benchmarkType + ".");
		}

		LOG.info("");
		LogUtil.logMultipleLines(benchmark.toString());
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
					graphName, baseGraphDir, baseGraphCacheDir));
		}

		return parsedGraphSets;
	}

	private void parseGraphSetsAndAlgorithmParameters(Collection<GraphSetParser> graphSetParsers)
			throws InvalidConfigurationException {
		foundGraphs = new HashMap<>();
		algorithmParameters = new HashMap<>();
		List<String> foundGraphs = new ArrayList<>();
		List<String> lostGraphs = new ArrayList<>();
		for (GraphSetParser parser : graphSetParsers) {
			Graph graph = parser.parseGraphSet();
			if (!graphExists(graph.getSourceGraph())) {
				lostGraphs.add(graph.getName());
				LOG.trace("Could not find file for graph \"" + graph.getName() + "\" at paths \"" +
						graph.getSourceGraph().getVertexFilePath() + "\" and \"" +
						graph.getSourceGraph().getEdgeFilePath() + "\". Skipping.");
				continue;
			}
			foundGraphs.add(graph.getName());
			this.foundGraphs.put(graph.getName(), graph);
			algorithmParameters.put(graph.getName(), parser.parseAlgorithmParameters());
		}
		LOG.info(String.format("Imported %s graph(s): %s.", foundGraphs.size(), foundGraphs));
		if(lostGraphs.size() > 0) {
			LOG.info(String.format("Failed to import %s graph(s): %s.", lostGraphs.size(), lostGraphs));
		}
	}

	private boolean graphExists(FormattedGraph formattedGraph) {
		return new File(formattedGraph.getVertexFilePath()).isFile() && new File(formattedGraph.getEdgeFilePath()).isFile();
	}



}
