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

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Arrays;

import science.atlarge.graphalytics.configuration.GraphalyticsExecutionException;
import science.atlarge.graphalytics.domain.benchmark.*;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import science.atlarge.graphalytics.domain.graph.LoadedGraph;
import science.atlarge.graphalytics.report.BenchmarkReportWriter;
import science.atlarge.graphalytics.report.html.HtmlBenchmarkReportGenerator;
import science.atlarge.graphalytics.report.result.BenchmarkMetric;
import science.atlarge.graphalytics.report.result.BenchmarkMetrics;
import science.atlarge.graphalytics.report.result.BenchmarkRunResult;
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
 * @author Wing Lung Ngai
 */
public class BenchmarkExecutor {
	private static final Logger LOG = LogManager.getLogger();
	private ExecutorService service;
	public static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";
	private final Benchmark benchmark;
	private final Platform platform;
	private final Plugins plugins;
	private BenchmarkReportWriter reportWriter;

	int finishedBenchmark;

	/**
	 * @param benchmark the suite of benchmarks to run
	 * @param platform       the platform instance to run the benchmarks on
	 * @param plugins        collection of loaded plugins
	 */
	public BenchmarkExecutor(Benchmark benchmark,
							 Platform platform,
							 Plugins plugins,
							 BenchmarkReportWriter reportWriter) {
		this.benchmark = benchmark;
		this.platform = platform;
		this.plugins = plugins;
		this.reportWriter = reportWriter;

		// Init the executor service;

		if(ProcessUtil.isNetworkPortAvailable(ExecutorService.getExecutorPort())) {
			ExecutorService.InitService(this);
		} else {
			LOG.error("The network port for the benchmark executor is not available");
			throw new GraphalyticsExecutionException("Failed to initialize benchmark executor. Benchmark aborted.");
		}
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

		HtmlBenchmarkReportGenerator htmlBenchmarkReportGenerator = new HtmlBenchmarkReportGenerator();

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

				LoadedGraph loadedGraph = null;

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
						loadedGraph = loadGraph(formattedGraph, fullGraphName);
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

						BenchmarkRunSetup benchmarkRunSetup = new BenchmarkRunSetup(benchmarkRun,
								benchmark.getBaseReportDir().resolve("log"),
								benchmark.getBaseOutputDir(), benchmark.getBaseValidationDir(),
								benchmark.isOutputRequired(), benchmark.isValidationRequired());

						RuntimeSetup runtimeSetup = new RuntimeSetup(loadedGraph);

						RunSpecification runSpecification = new RunSpecification(
								benchmarkRun, benchmarkRunSetup, runtimeSetup);

						benchmarkRunResult = runBenchmark(runSpecification);
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

					// (Over)write the benchmark report
					if (benchmark.isWriteResultsDirectlyEnabled()) {
						BenchmarkResult tmpBenchmarkResult = benchmarkSuiteResultBuilder.buildFromConfiguration(0);
						reportWriter.writeReport(htmlBenchmarkReportGenerator.generateReportFromResults(tmpBenchmarkResult));
					}

					// summarize result of the benchmark run.
					BenchmarkMetric loadTime = benchmarkRunResult.getMetrics().getLoadTime();
					BenchmarkMetric makespan = benchmarkRunResult.getMetrics().getMakespan();
					BenchmarkMetric procTime = benchmarkRunResult.getMetrics().getProcessingTime();

					LOG.info(String.format("Benchmark %s has %s, T_l=%s, T_m=%s, T_p=%s.",
							benchmarkRun.getId(),
							benchmarkRunResult.isSuccessful() ?
									"succeeded" : "failed (" + benchmarkRunResult.getFailures() +")",
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
				deleteGraph(loadedGraph, fullGraphName);
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
	 * The overview of the execution order of the benchmark run:
	 * [Executor] platform.load
	 * [Executor] plugin.prepare
	 * [Executor] platform.prepare
	 * [Runner] plugin.startup
	 * [Runner] platform.startup
	 * [Runner] platform.execute
	 * [Runner] platform.finalize
	 * [Runner] plugin.finalize
	 * [Executor] platform.terminate
	 * [Executor] plugin.terminate
	 *  [Executor][Granula] platform.enrichMetrics
	 * [Executor] plugin.postBenchmarkSuite
	 * [Executor] plugin.preReportGeneration
	 * @param runSpecification the description of the benchmark run.
	 * @return the result of a benchmark run.
	 */
	private BenchmarkRunResult runBenchmark(RunSpecification runSpecification) {
		BenchmarkFailures exeFailures = new BenchmarkFailures();

		BenchmarkRun benchmarkRun = runSpecification.getBenchmarkRun();
		BenchmarkRunSetup benchmarkRunSetup = runSpecification.getBenchmarkRunSetup();

		// ensure that the output directory exists, if needed
		createBenchmarkRunDirectories(runSpecification);

		LOG.info(String.format("Benchmark specification: [%s]", benchmarkRun));
		LOG.info(String.format("Benchmark configuration: [%s]", benchmarkRunSetup));
		LOG.info(String.format("Log directory: [%s]", benchmarkRunSetup.getLogDir()));
		LOG.info(String.format("Input file (vertex): [%s]", benchmarkRun.getFormattedGraph().getVertexFilePath()));
		LOG.info(String.format("Input file (edge): [%s]", benchmarkRun.getFormattedGraph().getEdgeFilePath()));
		LOG.info(String.format("Output directory: [%s]", benchmarkRunSetup.getOutputDir()));
		LOG.info(String.format("Validation directory: [%s]", benchmarkRunSetup.getValidationDir()));
		LOG.info("");


		BenchmarkRunStatus runnerStatus = new BenchmarkRunStatus(runSpecification);

		// execute the pre-benchmark steps of all plugins
		runnerStatus.setPrepared(false);
		plugins.prepare(runSpecification);
		try {
			platform.prepare(runSpecification);
			runnerStatus.setPrepared(true);
			LOG.info("The preparation for the benchmark succeed (if needed).");
		} catch (Exception e) {
			LOG.error("The preparation for the benchmark failed.", e);
			exeFailures.add(BenchmarkFailure.INI);
		}


		String runLogDir = benchmarkRunSetup.getLogDir().toAbsolutePath().toString();
		if(runnerStatus.isPrepared()) {

			if(!ProcessUtil.isNetworkPortAvailable(RunnerService.getRunnerPort())) {
				LOG.error(" The network port for the benchmark runner is not available");
				throw new GraphalyticsExecutionException("Failed to initialize benchmark runner. Benchmark aborted.");
			}

			// start the Benchmark Runner
			Process process = ProcessUtil.initRunner(
					BenchmarkRunner.class,
					Arrays.asList(platform.getPlatformName(), benchmarkRun.getId(), runLogDir));
			ProcessUtil.monitorProcess(process, benchmarkRun.getId());
			runnerStatus.setProcess(process);
			ExecutorService.runnerStatuses.put(benchmarkRun.getId(), runnerStatus);

			// wait for the runner for the registration, execution, validation, retreival steps.
			// terminate the runner when the time-out is reached.
			waitForInitialization(runnerStatus);
			if (runnerStatus.isInitialized()) {
				waitForExecution(runnerStatus, benchmark.getTimeout());

				if (runnerStatus.isRunned()) {
					waitForValidation(runnerStatus, benchmark.getTimeout());

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

		}


		BenchmarkRunResult benchmarkRunResult = runnerStatus.getBenchmarkRunResult();
		plugins.terminate(runSpecification, benchmarkRunResult);

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

	private LoadedGraph loadGraph(FormattedGraph formattedGraph, String fullGraphName) {
		LOG.info(String.format("----------------- Loading graph \"%s\" -----------------", fullGraphName));

		LoadedGraph loadedGraph = null;
		// load the graph
		try {
			loadedGraph = platform.loadGraph(formattedGraph);
		} catch (Exception e) {
			LOG.error("Failed to load graph \"" + fullGraphName + "\".", e);
			throw new GraphalyticsExecutionException("Several error in Graphalytics execution.");
		}

		LOG.info(String.format("----------------- Loaded graph \"%s\" -----------------", fullGraphName));
		return loadedGraph;
	}

	private void deleteGraph(LoadedGraph loadedGraph, String fullGraphName) {
		LOG.info(String.format("----------------- Deleting graph \"%s\" -----------------", fullGraphName));
		try {
			platform.deleteGraph(loadedGraph);
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
			service.sendTask(runnerInfo.getRunSpecification());
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

	private void waitForValidation(BenchmarkRunStatus runnerInfo, int maxDuration) {
		long startTime = System.currentTimeMillis();
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

		LOG.debug(String.format("Terminating benchmark run process(es)."));
		LOG.debug(String.format("Runner is %sinitialized and %srunned => platform process(es) %s running.",
				runnerInfo.isInitialized() ? "" : "not ",
				runnerInfo.isRunned() ? "" : "not ",
				runnerInfo.isInitialized() && !runnerInfo.isRunned() ? "still": "not"));

		try {
			if(runnerInfo.isInitialized() && !runnerInfo.isRunned()) {
				LOG.debug(String.format("Executing platform-specific \"terminate\" function."));
				platform.terminate(runnerInfo.getRunSpecification());
				LOG.debug(String.format("Executed platform-specific \"terminate\" function."));
			}
			BenchmarkRunner.terminateRunner(runnerInfo);
			runnerInfo.setTerminated(true);
			LOG.info(String.format("The benchmark run is sucessfully terminated."));
		} catch (Exception e) {
			LOG.error("Failed to terminate benchmark run.");
			throw new GraphalyticsExecutionException("Benchmark is aborted.", e);

		}

	}

	private void createBenchmarkRunDirectories(RunSpecification runSpecification) {
		BenchmarkRunSetup benchmarkRunSetup = runSpecification.getBenchmarkRunSetup();
		if (benchmarkRunSetup.isOutputRequired()) {
			try {
				Files.createDirectories(benchmarkRunSetup.getOutputDir());
			} catch (IOException e) {
				throw new IllegalStateException(
						String.format("Failed to create output directory \"%s\", skipping.",
								benchmarkRunSetup.getOutputDir().getParent()), e);
			}
		}
	}

	public void setService(ExecutorService service) {
		this.service = service;
	}
}
