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
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Arrays;

import science.atlarge.graphalytics.configuration.GraphalyticsExecutionException;
import science.atlarge.graphalytics.domain.benchmark.Benchmark;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import science.atlarge.graphalytics.report.result.BenchmarkMetric;
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

				BenchmarkFailures loadFailures = new BenchmarkFailures();

				long loadStartTime;
				long loadEndTime;
				BigDecimal loadTimeS = null;

				// Skip the graph if there are no benchmarks to run on it
				if (!benchmark.getBenchmarksForGraph(formattedGraph).isEmpty()) {

					LOG.info(String.format("Preprocessing graph %s for %s benchmark run(s).", fullGraphName, benchmarksForGraph));
					try {
						formatGraph(formattedGraph, fullGraphName);

						loadStartTime = System.currentTimeMillis();
						loadGraph(formattedGraph, fullGraphName);
						loadEndTime = System.currentTimeMillis();

						loadTimeS = (new BigDecimal(loadEndTime - loadStartTime))
								.divide(new BigDecimal(1000), 3, BigDecimal.ROUND_CEILING);;

						LOG.info(String.format("The loading process finished within %s seconds", (loadEndTime - loadStartTime) / 1000));
					} catch (Exception e) {
						int skippedBenchmark = benchmark.getBenchmarksForGraph(formattedGraph).size();
						LOG.error(String.format("Several error in Graphalytics execution: %s benchmark runs are skipped.", skippedBenchmark));
						loadFailures.add(BenchmarkFailure.DAT);
					}

				} else {
					LOG.info(String.format("Skipping formatted graph %s, not required for any benchmark run(s).", fullGraphName));
					continue;
				}
				LOG.info("");


				int numBenchmark =  benchmark.getBenchmarkRuns().size();
				// execute all benchmarks for this graph
				for (BenchmarkRun benchmarkRun : benchmark.getBenchmarksForGraph(formattedGraph)) {

					LOG.info("");
					LOG.info(String.format("============= Benchmark %s [%s/%s] =============",
							benchmarkRun.getId(), finishedBenchmark + 1, numBenchmark));

					BenchmarkRunResult benchmarkRunResult;
					if(loadFailures.hasNone()) {
						 benchmarkRunResult = runBenchmark(benchmarkRun);
						BenchmarkMetrics benchmarkMetrics = benchmarkRunResult.getMetrics();
						benchmarkMetrics.setLoadTime(new BenchmarkMetric(loadTimeS, "s"));

						if(benchmarkRunResult != null) {
							benchmarkSuiteResultBuilder.withBenchmarkResult(benchmarkRunResult);
						} else {
							benchmarkSuiteResultBuilder.withoutBenchmarkResult(benchmarkRun);
						}
					} else {
						benchmarkRunResult = BenchmarkRunResult.emptyBenchmarkRun(benchmarkRun);
						BenchmarkFailures failures = benchmarkRunResult.getFailures();
						failures.addAll(loadFailures);
						benchmarkSuiteResultBuilder.withFailedBenchmarkResult(benchmarkRunResult);
					}

					// summarize result of the benchmark run.
					BenchmarkMetric loadTime = benchmarkRunResult.getMetrics().getLoadTime();
					BenchmarkMetric makespan = benchmarkRunResult.getMetrics().getMakespan();
					BenchmarkMetric procTime = benchmarkRunResult.getMetrics().getProcessingTime();

					LOG.info(String.format("Benchmark %sis %s, T_l=%s, T_m=%s, T_p=%s.",
							benchmarkRun.getId(),
							benchmarkRunResult.isSuccessful() ?
									"succeed" : "failed (" + benchmarkRunResult.getFailures() +")",
							!loadTime.isNan() ? loadTime + loadTime.getUnit() : loadTime,
							!makespan.isNan() ? makespan + makespan.getUnit() : makespan,
							!procTime.isNan() ? procTime + procTime.getUnit() : procTime));

					LOG.info(String.format("============= Benchmark %s [%s/%s] =============",
							benchmarkRun.getId(), finishedBenchmark + 1, numBenchmark));
					LOG.info("");
					LOG.info("");
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
		BenchmarkFailures exeFailures = new BenchmarkFailures();

		// ensure that the output directory exists, if needed
		createBenchmarkRunDirectories(benchmarkRun);

		LOG.info(String.format("Benchmark specification: [%s]", benchmarkRun.getSpecification()));
		LOG.info(String.format("Benchmark configuration: [%s]", benchmarkRun.getConfigurations()));
		LOG.info(String.format("Log directory: [%s]", benchmarkRun.getLogDir()));
		LOG.info(String.format("Input file (vertex): [%s]", benchmarkRun.getFormattedGraph().getVertexFilePath()));
		LOG.info(String.format("Input file (edge): [%s]", benchmarkRun.getFormattedGraph().getEdgeFilePath()));
		LOG.info(String.format("Output directory: [%s]", benchmarkRun.getOutputDir()));
		LOG.info(String.format("Validation directory: [%s]", benchmarkRun.getValidationDir()));
		LOG.info("");


		BenchmarkRunStatus runnerStatus = new BenchmarkRunStatus(benchmarkRun);

		// execute the pre-benchmark steps of all plugins
		runnerStatus.setPrepared(false);
		plugins.preBenchmark(benchmarkRun);
		try {
			platform.prepare(benchmarkRun);
			runnerStatus.setPrepared(true);
			LOG.info("The preparation for the benchmark succeed (if needed).");
		} catch (Exception e) {
			LOG.error("The preparation for the benchmark failed.", e);
			exeFailures.add(BenchmarkFailure.INI);
		}


		if(runnerStatus.isPrepared()) {
			// start the Benchmark Runner
			Process process = ProcessUtil.initProcess(
					BenchmarkRunner.class,
					Arrays.asList(platform.getPlatformName(), benchmarkRun.getId()));
			ProcessUtil.monitorProcess(process, benchmarkRun.getId());
			runnerStatus.setProcess(process);
			ExecutorService.runnerStatuses.put(benchmarkRun.getId(), runnerStatus);

			// when the main process is shut down, also terminating the child processes.
			final Process p = runnerStatus.getProcess();
			Thread shutdownThread = new Thread() {
				public void run() { ProcessUtil.terminateProcess(p); }
			};
			Runtime r = Runtime.getRuntime();
			r.addShutdownHook(shutdownThread);

			// wait for the runner for the registration, execution, validation, retreival steps.
			// terminate the runner when the time-out is reached.
			waitForInitialization(runnerStatus);
			if (runnerStatus.isInitialized()) {
				waitForExecution(runnerStatus, benchmark.getTimeout());

				if (runnerStatus.isRunned()) {
					waitForValidation(runnerStatus);

					if (runnerStatus.isValidated()) {
						waitForRetrieval(runnerStatus);
						waitForTermination(runnerStatus);
					} else {
						waitForTermination(runnerStatus);
					}
				} else {
					waitForTermination(runnerStatus);
				}
			}
			else {
				waitForTermination(runnerStatus);
			}

			// when the main process is shut down, also terminating the child processes.
			r.removeShutdownHook(shutdownThread);
		}


		BenchmarkRunResult benchmarkRunResult = runnerStatus.getBenchmarkRunResult();
		plugins.postBenchmark(benchmarkRun, benchmarkRunResult);

		if(benchmarkRunResult == null) {
			benchmarkRunResult = BenchmarkRunResult.emptyBenchmarkRun(benchmarkRun);
		}

		BenchmarkFailures runFailures = runnerStatus.getRunFailures();
		BenchmarkFailures failures = benchmarkRunResult.getFailures();
		failures.addAll(runFailures);

		// check existence of metrics
		BenchmarkMetrics metrics = benchmarkRunResult.getMetrics();
		if(metrics.getMakespan().isNan()) {
			exeFailures.add(BenchmarkFailure.MET);
		}

		if(metrics.getProcessingTime().isNan()) {
			exeFailures.add(BenchmarkFailure.MET);
		}

		failures.addAll(exeFailures);


		finishedBenchmark++;

		return benchmarkRunResult;
	}

	private void formatGraph(FormattedGraph formattedGraph, String fullGraphName) {
		LOG.info(String.format("Formatting (Minimizing) graph \"%s\"", fullGraphName));

		// ensure that the graph input files exist (i.e. generate them from the Graph sources if needed)
		try {
			GraphFileManager.ensureGraphFilesExist(formattedGraph);
		} catch (Exception e) {
			LOG.error("Failed to format graph \"" + fullGraphName + "\".", e);
			throw new GraphalyticsExecutionException("Several error in Graphalytics execution.");
		}

		LOG.info(String.format("Formatted (Minimizing) graph \"%s\"", fullGraphName));
	}

	private void loadGraph(FormattedGraph formattedGraph, String fullGraphName) {
		LOG.info(String.format("----------------- Loading graph \"%s\" -----------------", fullGraphName));

		// load the graph
		try {
			platform.loadGraph(formattedGraph);
		} catch (Exception e) {
			LOG.error("Failed to load graph \"" + fullGraphName + "\".", e);
			throw new GraphalyticsExecutionException("Several error in Graphalytics execution.");
		}

		LOG.info(String.format("----------------- Loaded graph \"%s\" -----------------", fullGraphName));
	}

	private void deleteGraph(FormattedGraph formattedGraph, String fullGraphName) {
		LOG.info(String.format("----------------- Deleting graph \"%s\" -----------------", fullGraphName));
		try {
			platform.deleteGraph(formattedGraph);
		} catch (Exception e) {
			LOG.error(String.format("Failed to delete graph %s", fullGraphName));
			throw new GraphalyticsExecutionException("Fatal error in Graphalytics execution: the benchmark is aborted.", e);
		}

		LOG.info(String.format("----------------- Deleted graph \"%s\" -----------------", fullGraphName));
	}


	private void waitForInitialization(BenchmarkRunStatus runnerInfo) {
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
			runnerInfo.addFailure(BenchmarkFailure.INI);
		} else {
			LOG.info(String.format("The benchmark runner becomes ready within %s seconds.",
					TimeUtil.getTimeElapsed(startTime)));
			service.sendTask(runnerInfo.getBenchmarkRun());
		}
	}

	private void waitForExecution(BenchmarkRunStatus runnerInfo, int maxDuration) {
		long startTime = System.currentTimeMillis();
		while (!runnerInfo.isRunned()) {
			if(TimeUtil.waitFor(startTime, maxDuration, 1)) {
				break;
			}
			if(!runnerInfo.getRunFailures().hasNone()) {
				return;
			}
		}
		if(!runnerInfo.isRunned()) {
			LOG.error(String.format("Timeout is reached after %s seconds. " +
							"This benchmark run is forcibly terminated.", TimeUtil.getTimeElapsed(startTime)));
			runnerInfo.addFailure(BenchmarkFailure.TIM);
		} else {
			LOG.info(String.format("The execution process finished within %s seconds.",
					TimeUtil.getTimeElapsed(startTime)));
		}
	}

	private void waitForValidation(BenchmarkRunStatus runnerInfo) {
		long startTime = System.currentTimeMillis();
		long maxDuration = 3600 * 10;
		while (!runnerInfo.isValidated()) {
			if(TimeUtil.waitFor(startTime, maxDuration, 1)) {
				break;
			}
			if(!runnerInfo.getRunFailures().hasNone()) {
				return;
			}
		}
		if(!runnerInfo.isValidated()) {
			LOG.error(String.format("Timeout is reached after %s seconds. " +
							"The validation step failed.", TimeUtil.getTimeElapsed(startTime)));
			runnerInfo.addFailure(BenchmarkFailure.VAL);
		} else {
			LOG.info(String.format("The validation process finished within %s seconds.",
					TimeUtil.getTimeElapsed(startTime)));
		}

	}


	private void waitForRetrieval(BenchmarkRunStatus runnerInfo) {
		long startTime = System.currentTimeMillis();
		long maxDuration = 1000;
		while (!runnerInfo.isFinalized()) {
			if(TimeUtil.waitFor(startTime, maxDuration, 1)) {
				break;
			}
			if(!runnerInfo.getRunFailures().hasNone()) {
				return;
			}
		}
		if(!runnerInfo.isFinalized()) {
			LOG.error(String.format("Timeout is reached after %s seconds. No benchmark result retrieved.",
					TimeUtil.getTimeElapsed(startTime)));
		} else {
			LOG.info(String.format("The benchmark results are retrieved."));
		}
	}


	private void waitForTermination(BenchmarkRunStatus runnerInfo) {
		try {
			// TODO checking process termination by port availability.
			int runnerPort = RunnerService.getRunnerPort();
			ProcessUtil.terminateProcess(runnerInfo.getProcess(), runnerPort);
			runnerInfo.setTerminated(true);
			platform.terminate(runnerInfo.getBenchmarkRun());
			LOG.info(String.format("The benchmark run is terminated."));
		} catch (Exception e) {
			LOG.error("Failed to terminate benchmark.");
			throw new GraphalyticsExecutionException("Fatal error in Graphalytics execution: benchmark is aborted.", e);

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

	public void setService(ExecutorService service) {
		this.service = service;
	}
}
