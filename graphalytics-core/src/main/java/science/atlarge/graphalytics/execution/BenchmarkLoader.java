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
package science.atlarge.graphalytics.execution;

import science.atlarge.graphalytics.configuration.ConfigurationUtil;
import science.atlarge.graphalytics.configuration.GraphParser;
import science.atlarge.graphalytics.configuration.InvalidConfigurationException;
import science.atlarge.graphalytics.domain.algorithms.Algorithm;
import science.atlarge.graphalytics.domain.algorithms.AlgorithmParameters;
import science.atlarge.graphalytics.domain.benchmark.CustomBenchmark;
import science.atlarge.graphalytics.domain.benchmark.TestBenchmark;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import org.apache.commons.configuration.Configuration;
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
 * @author Mihai Capotă
 * @author Stijn Heldens
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public final class BenchmarkLoader {
	private static final Logger LOG = LogManager.getLogger();

	private static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";
	private static final String BENCHMARK_RUN_NAME = "benchmark.name";
	private static final String BENCHMARK_RUN_TYPE = "benchmark.type";
	private static final String BENCHMARK_RUN_TARGET_SCALE = "benchmark.standard.target-scale";
	private static final String BENCHMARK_RUN_OUTPUT_DIRECTORY_KEY = "graphs.output-directory";

	private static final String GRAPHS_VALIDATION_DIRECTORY_KEY = "graphs.validation-directory";
	private static final String GRAPHS_ROOT_DIRECTORY_KEY = "graphs.root-directory";
	private static final String GRAPHS_CACHE_DIRECTORY_KEY = "graphs.cache-directory";
	private static final String GRAPHS_NAMES_KEY = "graphs.names";

	private final Configuration benchmarkConfiguration;

	private Path outputDirectory;
	private String baseGraphDir;
	private String baseGraphCacheDir;
	private Path baseValidationDir;
	private Map<String, Graph> foundGraphs;
	private Map<String, Map<Algorithm, AlgorithmParameters>> algorithmParameters;

	private Benchmark benchmark = null;

	String platformName;

	public BenchmarkLoader(String platformName) {
		this.benchmarkConfiguration = ConfigurationUtil.loadConfiguration(BENCHMARK_PROPERTIES_FILE);
		this.platformName = platformName;
	}


	public Benchmark parse() throws InvalidConfigurationException {
		if (this.benchmark != null) {
			return this.benchmark;
		}

		outputDirectory = Paths.get(ConfigurationUtil.getString(benchmarkConfiguration, BENCHMARK_RUN_OUTPUT_DIRECTORY_KEY));

		baseGraphDir = ConfigurationUtil.getString(benchmarkConfiguration, GRAPHS_ROOT_DIRECTORY_KEY);
		baseGraphCacheDir = benchmarkConfiguration.getString(GRAPHS_CACHE_DIRECTORY_KEY,
				Paths.get(baseGraphDir, "cache").toString());
		baseValidationDir = Paths.get(benchmarkConfiguration.getString(GRAPHS_VALIDATION_DIRECTORY_KEY,
				baseGraphDir));

		Collection<GraphParser> graphParsers = constructGraphSetParsers();
		parseGraphSetsAndAlgorithmParameters(graphParsers);


		String benchmarkType = benchmarkConfiguration.getString(BENCHMARK_RUN_TYPE);
		String targetScale = benchmarkConfiguration.getString(BENCHMARK_RUN_TARGET_SCALE);
		Benchmark benchmark;
		Path baseReportDir = Paths.get("report/");
		switch (benchmarkType) {
			case "test":
				benchmark = new TestBenchmark(benchmarkType, platformName,
						baseReportDir, outputDirectory, baseValidationDir,
						foundGraphs, algorithmParameters);
				((TestBenchmark) benchmark).setup();
				break;

			case "standard":
				benchmark = new StandardBenchmark(benchmarkType, targetScale, platformName,
						baseReportDir, outputDirectory, baseValidationDir,
						foundGraphs, algorithmParameters);
				((StandardBenchmark) benchmark).setup();
				break;

			case "custom":
				benchmark = new CustomBenchmark(benchmarkType, platformName,
						baseReportDir, outputDirectory, baseValidationDir,
						foundGraphs, algorithmParameters);

				((CustomBenchmark) benchmark).setup();
				break;

			default:
				throw new IllegalArgumentException("Unkown benchmark type: " + benchmarkType + ".");
		}

		LOG.info("");
		return benchmark;
	}

	private Collection<GraphParser> constructGraphSetParsers()
			throws InvalidConfigurationException {
		// Get list of available graph sets
		String[] graphNames = ConfigurationUtil.getStringArray(benchmarkConfiguration, GRAPHS_NAMES_KEY);

		// Parse each graph set individually
		List<GraphParser> parsedGraphSets = new ArrayList<>(graphNames.length);
		for (String graphName : graphNames) {
			parsedGraphSets.add(new GraphParser(benchmarkConfiguration.subset("graph." + graphName),
					graphName, baseGraphDir, baseGraphCacheDir));
		}

		return parsedGraphSets;
	}

	private void parseGraphSetsAndAlgorithmParameters(Collection<GraphParser> graphParsers)
			throws InvalidConfigurationException {
		foundGraphs = new HashMap<>();
		algorithmParameters = new HashMap<>();
		List<String> foundGraphs = new ArrayList<>();
		List<String> lostGraphs = new ArrayList<>();
		for (GraphParser parser : graphParsers) {
			Graph graph = parser.parseGraph();
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
