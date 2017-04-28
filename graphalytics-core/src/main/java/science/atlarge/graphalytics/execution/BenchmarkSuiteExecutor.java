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

import java.io.IOException;
import java.nio.file.Files;

import science.atlarge.graphalytics.configuration.ConfigurationUtil;
import science.atlarge.graphalytics.domain.benchmark.Benchmark;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import science.atlarge.graphalytics.report.result.BenchmarkResult;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.report.result.BenchmarkSuiteResult;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.util.TimeUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import science.atlarge.graphalytics.plugin.Plugins;
import science.atlarge.graphalytics.util.GraphFileManager;

/**
 * Helper class for executing all benchmarks in a Benchmark on a specific Platform.
 *
 * @author Tim Hegeman
 */
public class BenchmarkSuiteExecutor {
	private static final Logger LOG = LogManager.getLogger();
	private ExecutorService service;


	public static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";

	private final Benchmark benchmark;
	private final Platform platform;
	private final Plugins plugins;
	private final int timeoutDuration;

	/**
	 * @param benchmark the suite of benchmarks to run
	 * @param platform       the platform instance to run the benchmarks on
	 * @param plugins        collection of loaded plugins
	 */
	public BenchmarkSuiteExecutor(Benchmark benchmark, Platform platform, Plugins plugins) {
		this.benchmark = benchmark;
		this.platform = platform;
		this.plugins = plugins;

		Configuration benchmarkConf = ConfigurationUtil.loadConfiguration(BENCHMARK_PROPERTIES_FILE);
		timeoutDuration = benchmarkConf.getInt("benchmark.run.timeout");

		// Init the executor service;
		ExecutorService.InitService(this);
	}


	/**
	 * Executes the Graphalytics benchmarkRun suite on the given platform. The benchmarks are grouped by graph so that each
	 * graph is uploaded to the platform exactly once. After executing all benchmarks for a specific graph, the graph
	 * is deleted from the platform.
	 *
	 * @return a BenchmarkSuiteResult object containing the gathered benchmark results and details
	 */
	public BenchmarkSuiteResult execute() {
		// TODO: Retrieve configuration for system, platform, and platform per benchmark

		// Use a BenchmarkSuiteResultBuilder to track the benchmark results gathered throughout execution
		BenchmarkSuiteResult.BenchmarkSuiteResultBuilder benchmarkSuiteResultBuilder = new BenchmarkSuiteResult.BenchmarkSuiteResultBuilder(benchmark);

		long totalStartTime = System.currentTimeMillis();
		int finishedBenchmark = 0;
		int numBenchmark =  benchmark.getBenchmarkRuns().size();


		LOG.info("");
		LOG.info(String.format("This benchmarkRun suite consists of %s benchmarks in total.", numBenchmark));


		for (Graph graph : benchmark.getGraphs()) {
			for (FormattedGraph formattedGraph : graph.getFormattedGraphs()) {


				LOG.debug(String.format("Preparing for %s benchmark runs that use graph %s.",
						benchmark.getBenchmarksForGraph(formattedGraph).size(), formattedGraph.getName()));


				LOG.info("");
				LOG.info(String.format("=======Start of Upload Graph %s =======", formattedGraph.getName()));

				// Skip the graph if there are no benchmarks to run on it
				if (benchmark.getBenchmarksForGraph(formattedGraph).isEmpty()) {
					continue;
				}

				// Ensure that the graph input files exist (i.e. generate them from the Graph sources if needed)
				try {
					GraphFileManager.ensureGraphFilesExist(formattedGraph);
				} catch (IOException ex) {
					LOG.error("Can not ensure that graph \"" + formattedGraph.getName() + "\" exists, skipping.", ex);
					continue;
				}

				// Upload the graph
				try {
					platform.uploadGraph(formattedGraph);
				} catch (Exception ex) {
					LOG.error("Failed to upload graph \"" + formattedGraph.getName() + "\", skipping.", ex);
					continue;
				}


				LOG.info(String.format("=======End of Upload Graph %s =======", formattedGraph.getName()));
				LOG.info("");

				// Execute all benchmarks for this graph
				for (BenchmarkRun benchmarkRun : benchmark.getBenchmarksForGraph(formattedGraph)) {
					// Ensure that the output directory exists, if needed
					if (benchmarkRun.isOutputRequired()) {
						try {
							Files.createDirectories(benchmarkRun.getOutputDir());
						} catch (IOException e) {
							LOG.error("Failed to create output directory \"" +
									benchmarkRun.getOutputDir().getParent() + "\", skipping.", e);
							continue;
						}
					}

					LOG.info("");
					LOG.info(String.format("=======Start of Benchmark %s [%s/%s]=======", benchmarkRun.getId(), finishedBenchmark + 1, numBenchmark));

					// Execute the pre-benchmark steps of all plugins
					plugins.preBenchmark(benchmarkRun);
					platform.prepare(benchmarkRun);


					LOG.info(String.format("Benchmark specification: [%s]", benchmarkRun.getSpecification()));
					LOG.info(String.format("Benchmark configuration: [%s]", benchmarkRun.getConfigurations()));
					LOG.info(String.format("Log directory: [%s]", benchmarkRun.getLogDir()));
					LOG.info(String.format("Output directory: [%s]", benchmarkRun.getOutputDir()));
					LOG.info(String.format("Validation file/directory: [%s]", benchmarkRun.getValidationDir()));
					Process process = BenchmarkRunner.InitializeJvmProcess(platform.getPlatformName(), benchmarkRun.getId());
					BenchmarkRunnerInfo runnerInfo = new BenchmarkRunnerInfo(benchmarkRun, process);
					ExecutorService.runnerInfos.put(benchmarkRun.getId(), runnerInfo);

					// wait for runner to get started.

					long waitingStarted;

					LOG.info("Initializing benchmark runner...");
					waitingStarted = System.currentTimeMillis();
					while (!runnerInfo.isRegistered()) {
						if(System.currentTimeMillis() - waitingStarted > 10 * 1000) {
							LOG.error("There is no response from the benchmarkRun runner. Benchmark run failed.");
							break;
						} else {
							TimeUtil.waitFor(1);
						}
					}
					LOG.info("The benchmark runner is already.");

					LOG.info("Running benchmark.");
					LOG.info("Waiting for completion...");
					waitingStarted = System.currentTimeMillis();
					while (!runnerInfo.isCompleted()) {
						if(System.currentTimeMillis() - waitingStarted > timeoutDuration * 1000) {
							LOG.error("Timeout is reached. This benchmark run is skipped.");
							break;
						} else {
							TimeUtil.waitFor(1);
						}
					}

					BenchmarkRunner.TerminateJvmProcess(process);

					BenchmarkResult benchmarkResult = runnerInfo.getBenchmarkResult();
					if(benchmarkResult != null) {
						benchmarkSuiteResultBuilder.withBenchmarkResult(benchmarkResult);

						long makespan = (benchmarkResult.getEndOfBenchmark().getTime() - benchmarkResult.getStartOfBenchmark().getTime());
						LOG.info(String.format("Benchmark %s %s (completed: %s, validated: %s), which took: %s ms.",
								benchmarkRun.getId(),
								benchmarkResult.isSuccessful() ? "succeed" : "failed",
								benchmarkResult.isCompleted(),
								benchmarkResult.isValidated(),
								makespan));
					} else {
						benchmarkSuiteResultBuilder.withoutBenchmarkResult(benchmarkRun);
						LOG.info(String.format("Benchmark %s %s (completed: %s, validated: %s).",
								benchmarkRun.getId(), "failed", false, false));
					}



					// Execute the post-benchmark steps of all plugins

					LOG.info(String.format("Cleaning up benchmark."));
					platform.cleanup(benchmarkRun);
					plugins.postBenchmark(benchmarkRun, benchmarkResult);

					finishedBenchmark++;
					LOG.info(String.format("=======End of Benchmark %s [%s/%s]=======", benchmarkRun.getId(), finishedBenchmark, numBenchmark));
					LOG.info("");
					LOG.info("");
				}

				// Delete the graph
				platform.deleteGraph(formattedGraph);
			}
		}
		service.terminate();

		long totalEndTime = System.currentTimeMillis();
		long totalDuration = totalEndTime - totalStartTime;

		// Construct the BenchmarkSuiteResult
		return benchmarkSuiteResultBuilder.buildFromConfiguration(totalDuration);
	}

	public void setService(ExecutorService service) {
		this.service = service;
	}
}
