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
import java.util.Arrays;

import science.atlarge.graphalytics.domain.benchmark.Benchmark;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import science.atlarge.graphalytics.report.result.BenchmarkMetrics;
import science.atlarge.graphalytics.report.result.BenchmarkRunResult;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.report.result.BenchmarkResult;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.util.ProcessUtil;
import science.atlarge.graphalytics.util.TimeUtil;
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

	int finishedBenchmark;

	/**
	 * @param benchmark the suite of benchmarks to run
	 * @param platform       the platform instance to run the benchmarks on
	 * @param plugins        collection of loaded plugins
	 */
	public BenchmarkExecutor(Benchmark benchmark, Platform platform, Plugins plugins) {
		this.benchmark = benchmark;
		this.platform = platform;
		this.plugins = plugins;

		// Init the executor service;
		ExecutorService.InitService(this);
	}


	/**
	 * Executes the Graphalytics benchmarkRun suite on the given platform. The benchmarks are grouped by graph so that each
	 * graph is uploaded to the platform exactly once. After executing all benchmarks for a specific graph, the graph
	 * is deleted from the platform.
	 *
	 * @return a BenchmarkResult object containing the gathered benchmark results and details
	 */
	public BenchmarkResult execute() {
		// TODO: Retrieve configuration for system, platform, and platform per benchmark

		// use a BenchmarkSuiteResultBuilder to track the benchmark results gathered throughout execution
		BenchmarkResult.BenchmarkSuiteResultBuilder benchmarkSuiteResultBuilder = new BenchmarkResult.BenchmarkSuiteResultBuilder(benchmark);

		long startTime = System.currentTimeMillis();
		finishedBenchmark = 0;


		LOG.info("");
		for (Graph graph : benchmark.getGraphs()) {

			LOG.info(String.format("Preparing graph \"%s\" with %s different formats: %s",
					graph.getName(),graph.getFormattedGraphs().size(), graph.listFormattedGraphs()));

			LOG.info("");
			LOG.info("");
			for (FormattedGraph formattedGraph : graph.getFormattedGraphs()) {
				String fullGraphName = String.format("\"%s:%s\"", graph.getName(), formattedGraph.getName());
				Integer benchmarksForGraph = benchmark.getBenchmarksForGraph(formattedGraph).size();

				// Skip the graph if there are no benchmarks to run on it
				if (!benchmark.getBenchmarksForGraph(formattedGraph).isEmpty()) {

					LOG.info(String.format("Preprocessing graph %s for %s benchmark run(s).", fullGraphName, benchmarksForGraph));
					formatGraph(formattedGraph, fullGraphName);
					loadGraph(formattedGraph, fullGraphName);
				} else {
					LOG.info(String.format("Skipping formatted graph %s, not required for any benchmark run(s).", fullGraphName));
					continue;
				}

				// execute all benchmarks for this graph
				for (BenchmarkRun benchmarkRun : benchmark.getBenchmarksForGraph(formattedGraph)) {
					BenchmarkRunResult benchmarkRunResult = runBenchmark(benchmarkRun);
					if(benchmarkRunResult != null) {
						benchmarkSuiteResultBuilder.withBenchmarkResult(benchmarkRunResult);
					} else {
						benchmarkSuiteResultBuilder.withoutBenchmarkResult(benchmarkRun);
					}
				}

				// delete the graph
				LOG.info(String.format("Deleting graph %s.", fullGraphName, benchmarksForGraph));
				deleteGraph(formattedGraph, fullGraphName);
				LOG.info("");
				LOG.info("");
			}
		}
		service.terminate();

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;

		// construct the BenchmarkResult
		return benchmarkSuiteResultBuilder.buildFromConfiguration(duration);
	}

	/**
	 * Executing a benchmark run.
	 * @param benchmarkRun the description of the benchmark run.
	 * @return the result of a benchmark run.
	 */
	private BenchmarkRunResult runBenchmark(BenchmarkRun benchmarkRun) {

		int numBenchmark =  benchmark.getBenchmarkRuns().size();
		BenchmarkFailure failure = BenchmarkFailure.NON;

		// ensure that the output directory exists, if needed
		createBenchmarkRunDirectories(benchmarkRun);

		LOG.info("");
		LOG.info(String.format("============= Benchmark %s [%s/%s] =============", benchmarkRun.getId(), finishedBenchmark + 1, numBenchmark));

		// execute the pre-benchmark steps of all plugins
		plugins.preBenchmark(benchmarkRun);
		platform.prepare(benchmarkRun);


		LOG.info(String.format("Benchmark specification: [%s]", benchmarkRun.getSpecification()));
		LOG.info(String.format("Benchmark configuration: [%s]", benchmarkRun.getConfigurations()));
		LOG.info(String.format("Log directory: [%s]", benchmarkRun.getLogDir()));
		LOG.info(String.format("Output directory: [%s]", benchmarkRun.getOutputDir()));
		LOG.info(String.format("Validation file/directory: [%s]", benchmarkRun.getValidationDir()));

		// start the Benchmark Runner
		Process process = ProcessUtil.initProcess(
				BenchmarkRunner.class,
				Arrays.asList(platform.getPlatformName(), benchmarkRun.getId()));
		ProcessUtil.monitorProcess(process, benchmarkRun.getId());

		BenchmarkRunnerInfo runnerInfo = new BenchmarkRunnerInfo(benchmarkRun, process);
		ExecutorService.runnerInfos.put(benchmarkRun.getId(), runnerInfo);

		// when the main process is shut down, also terminating the child processes.
		final Process p = runnerInfo.getProcess();
		Thread shutdownThread = new Thread() {
			public void run() { ProcessUtil.terminateProcess(p); }
		};
		Runtime r = Runtime.getRuntime();
		r.addShutdownHook(shutdownThread);

		// wait for the runner for the registration, execution, validation, retreival steps.
		// terminate the runner when the time-out is reached.
		waitForRegistration(runnerInfo);
		if (runnerInfo.isInitialized()) {
			waitForExecution(runnerInfo, benchmark.getTimeout());

			if (runnerInfo.isExecuted()) {
				waitForValidation(runnerInfo);

				if (runnerInfo.isValidated()) {
					waitForRetrieval(runnerInfo);
				} else {
					failure = BenchmarkFailure.VAL;
					waitForTermination(runnerInfo);
				}
			} else {
				failure = BenchmarkFailure.TIM;
				waitForTermination(runnerInfo);
			}
			waitForTermination(runnerInfo);
		}
		else {
			failure = BenchmarkFailure.INI;
			waitForTermination(runnerInfo);
		}

		// when the main process is shut down, also terminating the child processes.
		r.removeShutdownHook(shutdownThread);

		BenchmarkRunResult benchmarkRunResult = runnerInfo.getBenchmarkRunResult();
		plugins.postBenchmark(benchmarkRun, benchmarkRunResult);


		// check existence of metrics
		if(benchmarkRunResult != null) {
			BenchmarkMetrics metrics = benchmarkRunResult.getMetrics();
			if(metrics.getMakespan() == -1) {
				failure = BenchmarkFailure.MET;
			}

			if(metrics.getProcessingTime() == -1) {
				failure = BenchmarkFailure.MET;
			}
		}
		benchmarkRunResult = benchmarkRunResult.withBenchmarkFailure(failure);

		// summarize result of the benchmark run.
		if(benchmarkRunResult != null) {
			LOG.info(String.format("Benchmark %s %s (completed: %s, validated: %s), which took: %s ms.",
					benchmarkRun.getId(),
					benchmarkRunResult.isSuccessful() ? "succeed" : "failed",
					benchmarkRunResult.isCompleted(),
					benchmarkRunResult.isValidated(),
					benchmarkRunResult.getMetrics().getMakespan()));
		} else {
			LOG.info(String.format("Benchmark %s %s (completed: %s, validated: %s).",
					benchmarkRun.getId(), "failed", false, false));
		}


		LOG.info(String.format("============= Benchmark %s [%s/%s] =============",
				benchmarkRun.getId(), finishedBenchmark, numBenchmark));
		LOG.info("");
		LOG.info("");
		finishedBenchmark++;



		return benchmarkRunResult;
	}

	private void formatGraph(FormattedGraph formattedGraph, String fullGraphName) {
		LOG.info(String.format("Formatting (Minimizing) graph \"%s\"", fullGraphName));

		// Ensure that the graph input files exist (i.e. generate them from the Graph sources if needed)
		try {
			GraphFileManager.ensureGraphFilesExist(formattedGraph);
		} catch (IOException ex) {
			LOG.error("Can not ensure that graph \"" + fullGraphName + "\" exists, skipping.", ex);
			return;
		}

		LOG.info(String.format("Formatted (Minimizing) graph \"%s\"", fullGraphName));
	}

	private void loadGraph(FormattedGraph formattedGraph, String fullGraphName) {
		LOG.info(String.format("----------------- Loading graph \"%s\" -----------------", fullGraphName));

		// Upload the graph
		try {
			platform.loadGraph(formattedGraph);
		} catch (Exception ex) {
			LOG.error("Failed to upload graph \"" + fullGraphName + "\", skipping.", ex);
			return;
		}

		LOG.info(String.format("----------------- Loaded graph \"%s\" -----------------", fullGraphName));
		LOG.info("");

	}

	private void deleteGraph(FormattedGraph formattedGraph, String fullGraphName) {
		LOG.info(String.format("----------------- Deleting graph \"%s\" -----------------", fullGraphName));
		platform.deleteGraph(formattedGraph);
		LOG.info(String.format("----------------- Deleted graph \"%s\" -----------------", fullGraphName));
	}


	private void waitForRegistration(BenchmarkRunnerInfo runnerInfo) {
		long startTime = System.currentTimeMillis();
		long maxDuration = 20;
		while (!runnerInfo.isInitialized()) {
			if(TimeUtil.waitFor(startTime, maxDuration, 1)) {
				break;
			}
		}
		if(!runnerInfo.isInitialized()) {
			LOG.error(String.format("No response from the runner after %s seconds. " +
					"Benchmark run skipped.", maxDuration));
		} else {
			LOG.info(String.format("The benchmark runner is ready after %s seconds.",
					TimeUtil.getTimeElapsed(startTime)));
		}
	}

	private void waitForExecution(BenchmarkRunnerInfo runnerInfo, int maxDuration) {
		long startTime = System.currentTimeMillis();
		while (!runnerInfo.isExecuted()) {
			if(TimeUtil.waitFor(startTime, maxDuration, 1)) {
				break;
			}
		}
		if(!runnerInfo.isExecuted()) {
			LOG.error(String.format("Timeout is reached after %s seconds. " +
							"This benchmark run is forcibly terminated.", TimeUtil.getTimeElapsed(startTime)));
		} else {
			LOG.info(String.format("The execution process finished after %s seconds.",
					TimeUtil.getTimeElapsed(startTime)));
		}
	}

	private void waitForValidation(BenchmarkRunnerInfo runnerInfo) {
		long startTime = System.currentTimeMillis();
		long maxDuration = 3600 * 10;
		while (!runnerInfo.isValidated()) {
			if(TimeUtil.waitFor(startTime, maxDuration, 1)) {
				break;
			}
		}
		if(!runnerInfo.isValidated()) {
			LOG.error(String.format("Timeout is reached after %s seconds. " +
							"The validation step failed.", TimeUtil.getTimeElapsed(startTime)));
		} else {
			LOG.info(String.format("The validation process finished after %s seconds.",
					TimeUtil.getTimeElapsed(startTime)));
		}

	}


	private void waitForRetrieval(BenchmarkRunnerInfo runnerInfo) {
		long startTime = System.currentTimeMillis();
		long maxDuration = 1000;
		while (!runnerInfo.isCompleted()) {
			if(TimeUtil.waitFor(startTime, maxDuration, 1)) {
				break;
			}
		}
		if(!runnerInfo.isCompleted()) {
			LOG.error(String.format("Timeout is reached after %s seconds. No benchmark result retrieved.",
					TimeUtil.getTimeElapsed(startTime)));
		} else {
			LOG.info(String.format("The benchmark results are retrieved."));
		}
	}


	private void waitForTermination(BenchmarkRunnerInfo runnerInfo) {

		LOG.info(String.format("The benchmark run is terminated."));
		ProcessUtil.terminateProcess(runnerInfo.getProcess());
		runnerInfo.setTerminated(true);
		platform.terminate(runnerInfo.getBenchmarkRun());
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

	public void setService(ExecutorService service) {
		this.service = service;
	}
}
