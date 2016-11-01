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

	private final BenchmarkSuite benchmarkSuite;
	private final Platform platform;
	private final Plugins plugins;

	/**
	 * @param benchmarkSuite the suite of benchmarks to run
	 * @param platform       the platform instance to run the benchmarks on
	 * @param plugins        collection of loaded plugins
	 */
	public BenchmarkSuiteExecutor(BenchmarkSuite benchmarkSuite, Platform platform, Plugins plugins) {
		this.benchmarkSuite = benchmarkSuite;
		this.platform = platform;
		this.plugins = plugins;

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

		int finishedBenchmark = 0;
		int numBenchmark =  benchmarkSuite.getBenchmarks().size();


		LOG.info("");
		LOG.info(String.format("This benchmark suite consists of %s benchmarks in total.", numBenchmark));


		for (GraphSet graphSet : benchmarkSuite.getGraphSets()) {
			for (Graph graph : graphSet.getGraphs()) {

				LOG.debug(String.format("Preparing for %s benchmark rus for graph %s.",
						benchmarkSuite.getBenchmarksForGraph(graph).size(), graph.getName()));

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
					LOG.info(String.format("=======Start of Benchmark %s=======", benchmark.getId()));

					// Execute the pre-benchmark steps of all plugins
					plugins.preBenchmark(benchmark);


					LOG.info(String.format("Benchmark %s started.", benchmarkText));

					Process process = BenchmarkRunner.InitializeJvmProcess(platform.getName(), benchmark.getId());
					BenchmarkRunnerInfo runnerInfo = new BenchmarkRunnerInfo(benchmark, process);
					ExecutorService.runnerInfos.put(benchmark.getId(), runnerInfo);

					// wait for runner to get started.

					LOG.info("Waiting for runner...");
					while (!runnerInfo.isRegistered()) {
						TimeUtility.waitFor(1);
					}

					LOG.info("Running benchmark...");

					LOG.info("Waiting for completion...");
					while (!runnerInfo.isCompleted()) {
						TimeUtility.waitFor(1);
					}

					BenchmarkRunner.TerminateJvmProcess(process);

					BenchmarkResult benchmarkResult = runnerInfo.getBenchmarkResult();

					benchmarkSuiteResultBuilder.withBenchmarkResult(benchmarkResult);

					LOG.info(String.format("Benchmark %s ended.", benchmarkText));

					long makespan = (benchmarkResult.getEndOfBenchmark().getTime() - benchmarkResult.getStartOfBenchmark().getTime());
					LOG.info(String.format("Benchmark %s is %s (completed: %s, validated: %s), which took: %s ms.",
							benchmark.getId(),
							benchmarkResult.isSuccessful() ? "succeed" : "failed",
							benchmarkResult.isCompleted(),
							benchmarkResult.isValidated(),
							makespan));

					// Execute the post-benchmark steps of all plugins
					plugins.postBenchmark(benchmark, benchmarkResult);
					finishedBenchmark++;
					LOG.info(String.format("Benchmark completion: %s/%s", finishedBenchmark, numBenchmark));
					LOG.info(String.format("=======End of Benchmark %s=======", benchmark.getId()));
					LOG.info("");
				}

				// Delete the graph
				platform.deleteGraph(graph.getName());
			}
			service.terminate();
		}

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
				platform.getPlatformConfiguration());
	}

	public void setService(ExecutorService service) {
		this.service = service;
	}
}
