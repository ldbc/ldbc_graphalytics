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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import nl.tudelft.graphalytics.network.ExecutorService;
import nl.tudelft.graphalytics.util.TimeUtility;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.tudelft.graphalytics.domain.Benchmark;
import nl.tudelft.graphalytics.domain.BenchmarkResult;
import nl.tudelft.graphalytics.domain.BenchmarkSuite;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult.BenchmarkSuiteResultBuilder;
import nl.tudelft.graphalytics.domain.Graph;
import nl.tudelft.graphalytics.domain.GraphSet;
import nl.tudelft.graphalytics.domain.NestedConfiguration;
import nl.tudelft.graphalytics.domain.SystemDetails;
import nl.tudelft.graphalytics.plugin.Plugins;
import nl.tudelft.graphalytics.util.GraphFileManager;

/**
 * Helper class for executing all benchmarks in a BenchmarkSuite on a specific Platform.
 *
 * @author Tim Hegeman
 */
public class BenchmarkSuiteExecutor {
	private static final Logger LOG = LogManager.getLogger();
	private ExecutorService service;


	public static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";

	private final BenchmarkSuite benchmarkSuite;
	private final Platform platform;
	private final Plugins plugins;
	private final int timeoutDuration;

	/**
	 * @param benchmarkSuite the suite of benchmarks to run
	 * @param platform       the platform instance to run the benchmarks on
	 * @param plugins        collection of loaded plugins
	 */
	public BenchmarkSuiteExecutor(BenchmarkSuite benchmarkSuite, Platform platform, Plugins plugins) {
		this.benchmarkSuite = benchmarkSuite;
		this.platform = platform;
		this.plugins = plugins;

		try {
			Configuration benchmarkConf = new PropertiesConfiguration(BENCHMARK_PROPERTIES_FILE);
			timeoutDuration = benchmarkConf.getInt("benchmark.run.timeout");
		} catch (ConfigurationException e) {
			e.printStackTrace();
			throw new IllegalStateException("Failed to load configurations from " + BENCHMARK_PROPERTIES_FILE);
		}

		// Init the executor service;
		ExecutorService.InitService(this);
	}


	/**
	 * Executes the Graphalytics benchmark suite on the given platform. The benchmarks are grouped by graph so that each
	 * graph is uploaded to the platform exactly once. After executing all benchmarks for a specific graph, the graph
	 * is deleted from the platform.
	 *
	 * @return a BenchmarkSuiteResult object containing the gathered benchmark results and details
	 */
	public BenchmarkSuiteResult execute() {
		// TODO: Retrieve configuration for system, platform, and platform per benchmark

		// Use a BenchmarkSuiteResultBuilder to track the benchmark results gathered throughout execution
		BenchmarkSuiteResultBuilder benchmarkSuiteResultBuilder = new BenchmarkSuiteResultBuilder(benchmarkSuite);

		long totalStartTime = System.currentTimeMillis();
		int finishedBenchmark = 0;
		int numBenchmark =  benchmarkSuite.getBenchmarks().size();


		LOG.info("");
		LOG.info(String.format("This benchmark suite consists of %s benchmarks in total.", numBenchmark));


		for (GraphSet graphSet : benchmarkSuite.getGraphSets()) {
			for (Graph graph : graphSet.getGraphs()) {


				LOG.debug(String.format("Preparing for %s benchmark runs that use graph %s.",
						benchmarkSuite.getBenchmarksForGraph(graph).size(), graph.getName()));


				LOG.info("");
				LOG.info(String.format("=======Start of Upload Graph %s =======", graph.getName()));

				// Skip the graph if there are no benchmarks to run on it
				if (benchmarkSuite.getBenchmarksForGraph(graph).isEmpty()) {
					continue;
				}

				// Ensure that the graph input files exist (i.e. generate them from the GraphSet sources if needed)
				try {
					GraphFileManager.ensureGraphFilesExist(graph);
				} catch (IOException ex) {
					LOG.error("Can not ensure that graph \"" + graph.getName() + "\" exists, skipping.", ex);
					continue;
				}

				// Upload the graph
				try {
					platform.uploadGraph(graph);
				} catch (Exception ex) {
					LOG.error("Failed to upload graph \"" + graph.getName() + "\", skipping.", ex);
					continue;
				}


				LOG.info(String.format("=======End of Upload Graph %s =======", graph.getName()));
				LOG.info("");

				// Execute all benchmarks for this graph
				for (Benchmark benchmark : benchmarkSuite.getBenchmarksForGraph(graph)) {
					// Ensure that the output directory exists, if needed
					if (benchmark.isOutputRequired()) {
						try {
							Files.createDirectories(Paths.get(benchmark.getOutputPath()).getParent());
						} catch (IOException e) {
							LOG.error("Failed to create output directory \"" +
									Paths.get(benchmark.getOutputPath()).getParent() + "\", skipping.", e);
							continue;
						}
					}

					String benchmarkText = String.format("%s:\"%s on %s\"", benchmark.getId(), benchmark.getAlgorithm().getAcronym(), graphSet.getName());

					LOG.info("");
					LOG.info(String.format("=======Start of Benchmark %s [%s/%s]=======", benchmark.getId(), finishedBenchmark + 1, numBenchmark));

					// Execute the pre-benchmark steps of all plugins
					plugins.preBenchmark(benchmark);


					LOG.info(String.format("Benchmark %s started.", benchmarkText));

					Process process = BenchmarkRunner.InitializeJvmProcess(platform.getName(), benchmark.getId());
					BenchmarkRunnerInfo runnerInfo = new BenchmarkRunnerInfo(benchmark, process);
					ExecutorService.runnerInfos.put(benchmark.getId(), runnerInfo);

					// wait for runner to get started.

					long waitingStarted;

					LOG.info("Initializing benchmark runner...");
					waitingStarted = System.currentTimeMillis();
					while (!runnerInfo.isRegistered()) {
						if(System.currentTimeMillis() - waitingStarted > 10 * 1000) {
							LOG.error("There is no response from the benchmark runner. Benchmark run failed.");
							break;
						} else {
							TimeUtility.waitFor(1);
						}
					}
					LOG.info("The benchmark runner is initialized.");

					LOG.info("Running benchmark...");
					LOG.info("Benchmark logs are stored at: \"" + benchmark.getLogPath() +"\".");
					LOG.info("Waiting for completion... (Timeout after " + timeoutDuration + " seconds)");
					waitingStarted = System.currentTimeMillis();
					while (!runnerInfo.isCompleted()) {
						if(System.currentTimeMillis() - waitingStarted > timeoutDuration * 1000) {
							LOG.error("Timeout is reached. This benchmark run is skipped.");
							break;
						} else {
							TimeUtility.waitFor(1);
						}
					}

					BenchmarkRunner.TerminateJvmProcess(process);

					BenchmarkResult benchmarkResult = runnerInfo.getBenchmarkResult();
					if(benchmarkResult != null) {
						benchmarkSuiteResultBuilder.withBenchmarkResult(benchmarkResult);

						long makespan = (benchmarkResult.getEndOfBenchmark().getTime() - benchmarkResult.getStartOfBenchmark().getTime());
						LOG.info(String.format("Benchmark %s %s (completed: %s, validated: %s), which took: %s ms.",
								benchmark.getId(),
								benchmarkResult.isSuccessful() ? "succeed" : "failed",
								benchmarkResult.isCompleted(),
								benchmarkResult.isValidated(),
								makespan));
					} else {
						benchmarkSuiteResultBuilder.withoutBenchmarkResult(benchmark);
						LOG.info(String.format("Benchmark %s %s (completed: %s, validated: %s).",
								benchmark.getId(), "failed", false, false));
					}

					LOG.info(String.format("Benchmark %s ended.", benchmarkText));


					// Execute the post-benchmark steps of all plugins

					LOG.info(String.format("Cleaning up %s.", benchmarkText));
					platform.cleanup(benchmark);
					plugins.postBenchmark(benchmark, benchmarkResult);

					finishedBenchmark++;
					LOG.info(String.format("=======End of Benchmark %s [%s/%s]=======", benchmark.getId(), finishedBenchmark, numBenchmark));
					LOG.info("");
				}

				// Delete the graph
				platform.deleteGraph(graph.getName());
			}
		}
		service.terminate();

		long totalEndTime = System.currentTimeMillis();
		long totalDuration = totalEndTime - totalStartTime;

		// Dump the used configuration
		NestedConfiguration benchmarkConfiguration = NestedConfiguration.empty();
		try {
			Configuration configuration = new PropertiesConfiguration("benchmark.properties");
			benchmarkConfiguration = NestedConfiguration.fromExternalConfiguration(configuration,
					"benchmark.properties");
		} catch (ConfigurationException e) {
			// Already reported during loading of benchmark
		}

		// Construct the BenchmarkSuiteResult
		return benchmarkSuiteResultBuilder.buildFromConfiguration(SystemDetails.empty(),
				benchmarkConfiguration,
				platform.getPlatformConfiguration(), totalDuration);
	}

	public void setService(ExecutorService service) {
		this.service = service;
	}
}
