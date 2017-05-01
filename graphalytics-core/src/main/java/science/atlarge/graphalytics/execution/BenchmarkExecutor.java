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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import science.atlarge.graphalytics.plugin.Plugins;
import science.atlarge.graphalytics.util.GraphFileManager;

/**
 * Helper class for executing all benchmarks in a Benchmark on a specific Platform.
 *
 * @author Tim Hegeman
 */
public class BenchmarkExecutor {
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
	public BenchmarkExecutor(Benchmark benchmark, Platform platform, Plugins plugins) {
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
//		LOG.info(String.format("This benchmarkRun consists of %s benchmarks in total.", numBenchmark));


		for (Graph graph : benchmark.getGraphs()) {

			LOG.info(String.format("Setting up graph \"%s\" with %s different formats: %s",
					graph.getName(),graph.getFormattedGraphs().size(), graph.listFormattedGraphs()));

			LOG.info("");

			for (FormattedGraph formattedGraph : graph.getFormattedGraphs()) {
				String fullGraphName = String.format("\"%s:%s\"", graph.getName(), formattedGraph.getName());
				Integer benchmarksForGraph = benchmark.getBenchmarksForGraph(formattedGraph).size();

				LOG.info(String.format("Uploading formatted graph %s for %s benchmark run(s).", fullGraphName, benchmarksForGraph));
				uploadFormattedGraph(formattedGraph, fullGraphName);


				// Execute all benchmarks for this graph
				for (BenchmarkRun benchmarkRun : benchmark.getBenchmarksForGraph(formattedGraph)) {
					// Ensure that the output directory exists, if needed
					createBenchmarkRunDirectories(benchmarkRun);

					LOG.info("");
					LOG.info(String.format("======= Start of Benchmark %s [%s/%s] =======", benchmarkRun.getId(), finishedBenchmark + 1, numBenchmark));

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

					// wait for the runner to get started.
					long registrStartTime;
					LOG.info("Initializing benchmark runner...");
					registrStartTime = System.currentTimeMillis();
					Integer registrWaitTime = 10;
					while (!runnerInfo.isRegistered()) {
						if(TimeUtil.waitFor(registrStartTime, registrWaitTime, 1)) {
							break;
						}
					}

					// if the runner does not response, skip the benchmark.
					if(!runnerInfo.isRegistered()) {
						LOG.error(String.format("No response from the runner after %s seconds. Benchmark run skipped.", registrWaitTime));

						BenchmarkRunner.TerminateJvmProcess(process);

						BenchmarkResult benchmarkResult = null;
						processBenchmarkResult(benchmarkSuiteResultBuilder, benchmarkRun, benchmarkResult);;

						LOG.info(String.format("Cleaning up benchmark."));
						plugins.postBenchmark(benchmarkRun, benchmarkResult);
						platform.cleanup(benchmarkRun);

						finishedBenchmark++;
						LOG.info(String.format("======= End of Benchmark %s [%s/%s] =======", benchmarkRun.getId(), finishedBenchmark, numBenchmark));
						LOG.info("");
						LOG.info("");
					} // if the runner registers itself, continue with the benchmark.
					else {
						LOG.info(String.format("The benchmark runner becomes standby after %s seconds.", TimeUtil.getTimeElapsed(registrStartTime)));
						LOG.info("Waiting for benchmark execution...");

						waitForExecution(runnerInfo);
						waitForValidation(runnerInfo);
						waitForRetrieval(runnerInfo);

						BenchmarkRunner.TerminateJvmProcess(process);

						BenchmarkResult benchmarkResult = runnerInfo.getBenchmarkResult();
						processBenchmarkResult(benchmarkSuiteResultBuilder, benchmarkRun, benchmarkResult);

						// Execute the post-benchmark steps of all plugins
						LOG.info(String.format("Cleaning up benchmark."));
						platform.cleanup(benchmarkRun);
						plugins.postBenchmark(benchmarkRun, benchmarkResult);

						finishedBenchmark++;
						LOG.info(String.format("=======End of Benchmark %s [%s/%s]=======", benchmarkRun.getId(), finishedBenchmark, numBenchmark));
						LOG.info("");
						LOG.info("");
					}
				}

				// Delete the graph

				LOG.info(String.format("Deleting formatted graph %s.", fullGraphName, benchmarksForGraph));
				deleteFormattedGraph(formattedGraph, fullGraphName);

			}
		}
		service.terminate();

		long totalEndTime = System.currentTimeMillis();
		long totalDuration = totalEndTime - totalStartTime;

		// Construct the BenchmarkSuiteResult
		return benchmarkSuiteResultBuilder.buildFromConfiguration(totalDuration);
	}

	private void waitForValidation(BenchmarkRunnerInfo runnerInfo) {
		// validating benchmark
		long validationStartTime = System.currentTimeMillis();
		while (!runnerInfo.isValidated()) {
			if(TimeUtil.waitFor(validationStartTime, 3600, 1)) {
				break;
			}
		}
		if(!runnerInfo.isValidated()) {
			LOG.error(String.format("Timeout is reached after %s seconds. This benchmark cannot be validated.",
					TimeUtil.getTimeElapsed(validationStartTime)));
		} else {
			LOG.info(String.format("The validation process finished after %s seconds.", TimeUtil.getTimeElapsed(validationStartTime)));
		}

	}

	private void waitForRetrieval(BenchmarkRunnerInfo runnerInfo) {
		// retrieving benchmark result.
		long completionStartTime = System.currentTimeMillis();
		while (!runnerInfo.isRetrieved()) {
			if(TimeUtil.waitFor(completionStartTime, 1000, 1)) {
				break;
			}
		}
		if(!runnerInfo.isRetrieved()) {
			LOG.error(String.format("Timeout is reached after %s seconds. No benchmark result retrieved.",
					TimeUtil.getTimeElapsed(completionStartTime)));
		} else {
			LOG.info(String.format("The benchmark results are retrieved."));
		}
	}

	private void waitForExecution(BenchmarkRunnerInfo runnerInfo) {
		// executing benchmark
		long executionStartTime = System.currentTimeMillis();
		while (!runnerInfo.isExecuted()) {
			if(TimeUtil.waitFor(executionStartTime, timeoutDuration, 1)) {
				break;
			}
		}
		if(!runnerInfo.isExecuted()) {
			LOG.error(String.format("Timeout is reached after %s seconds. This benchmark run is forcibly terminated.",
					TimeUtil.getTimeElapsed(executionStartTime)));
		} else {
			LOG.info(String.format("The execution process finished after %s seconds.", TimeUtil.getTimeElapsed(executionStartTime)));
		}
	}

	private void createBenchmarkRunDirectories(BenchmarkRun benchmarkRun) {
		if (benchmarkRun.isOutputRequired()) {
			try {
				Files.createDirectories(benchmarkRun.getOutputDir());
			} catch (IOException e) {
				throw new IllegalStateException(
						String.format("Failed to create output directory \"%s\", skipping.",
								benchmarkRun.getOutputDir().getParent()), e);
			}
		}
	}

	private void uploadFormattedGraph(FormattedGraph formattedGraph, String fullGraphName) {
		LOG.info(String.format("------- Start of Upload Graph \"%s\" -------", fullGraphName));

		// Skip the graph if there are no benchmarks to run on it
		if (benchmark.getBenchmarksForGraph(formattedGraph).isEmpty()) {
			return;
		}

		// Ensure that the graph input files exist (i.e. generate them from the Graph sources if needed)
		try {
			GraphFileManager.ensureGraphFilesExist(formattedGraph);
		} catch (IOException ex) {
			LOG.error("Can not ensure that graph \"" + fullGraphName + "\" exists, skipping.", ex);
			return;
		}

		// Upload the graph
		try {
			platform.uploadGraph(formattedGraph);
		} catch (Exception ex) {
			LOG.error("Failed to upload graph \"" + fullGraphName + "\", skipping.", ex);
			return;
		}


		LOG.info(String.format("------- End of Upload Graph \"%s\" -------", fullGraphName));
		LOG.info("");

	}


	private void deleteFormattedGraph(FormattedGraph formattedGraph, String fullGraphName) {
		LOG.info(String.format("------- Start of Delete Graph \"%s\" -------", fullGraphName));
		platform.deleteGraph(formattedGraph);
		LOG.info(String.format("------- End of Delete Graph \"%s\" -------", fullGraphName));
		LOG.info("");
	}


	private void processBenchmarkResult(BenchmarkSuiteResult.BenchmarkSuiteResultBuilder benchmarkSuiteResultBuilder, BenchmarkRun benchmarkRun, BenchmarkResult benchmarkResult) {
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

	}

	public void setService(ExecutorService service) {
		this.service = service;
	}
}
