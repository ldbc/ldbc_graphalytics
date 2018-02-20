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

import org.apache.logging.log4j.Level;
import science.atlarge.graphalytics.configuration.GraphalyticsExecutionException;
import science.atlarge.graphalytics.plugin.Plugins;
import science.atlarge.graphalytics.report.result.BenchmarkMetric;
import science.atlarge.graphalytics.util.LogUtil;
import science.atlarge.graphalytics.configuration.PlatformParser;
import science.atlarge.graphalytics.report.result.BenchmarkMetrics;
import science.atlarge.graphalytics.report.result.BenchmarkRunResult;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.util.ProcessUtil;
import science.atlarge.graphalytics.util.TimeUtil;
import science.atlarge.graphalytics.validation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.validation.rule.EpsilonValidationRule;
import science.atlarge.graphalytics.validation.rule.ValidationRule;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.readAllBytes;

/**
 *
 * @author Wing Lung Ngai
 */
public class BenchmarkRunner {

	private static Logger LOG;

	private RunnerService service;
	private Plugins plugins;

	Platform platform;
	String benchmarkId;

	BenchmarkStatus benchmarkStatus;


	public static void main(String[] args) throws IOException {
		// Get an instance of the platform integration code


		LogUtil.intializeLoggers();
		LogUtil.appendSimplifiedConsoleLogger(Level.TRACE);
		LOG = LogManager.getLogger();

		LOG.info("Initializing Benchmark Runner.");

		try {
			BenchmarkRunner executor = new BenchmarkRunner();
			registerRunnerProcessId(Paths.get(args[2]));
			executor.platform = PlatformParser.loadPlatformFromCommandLineArgs();
			executor.benchmarkId = args[1];
			executor.setPlugins(Plugins.discoverPluginsOnClasspath(executor.getPlatform(), null, null));

			RunnerService.InitService(executor);
		} catch (Exception e) {
			LOG.error(e);
			e.printStackTrace();
			System.exit(1);
		}
	}

	public BenchmarkRunner() {
		benchmarkStatus = new BenchmarkStatus();
	}

	public void startup(RunSpecification runSpecification) throws Exception {
		platform.startup(runSpecification);
	}

	public BenchmarkMetrics finalize(RunSpecification runSpecification) throws Exception {
		return platform.finalize(runSpecification);
	}


	public boolean run(RunSpecification runSpecification) {
		boolean runned = false;
		Platform platform = getPlatform();

		BenchmarkRun benchmarkRun = runSpecification.getBenchmarkRun();

		LOG.info(String.format("Runner executing benchmark %s.", benchmarkRun.getId()));

		// Start the timer
		benchmarkStatus.setStartOfBenchmark();

		// Execute the benchmark and collect the result
		try {
			platform.run(runSpecification);
			runned = true;
		} catch(Exception ex) {
			LOG.error("Algorithm \"" + benchmarkRun.getAlgorithm().getName() + "\" on graph \"" +
					benchmarkRun.getFormattedGraph().getGraph().getName() + " failed to complete:", ex);
		}

		// Stop the timer
		benchmarkStatus.setEndOfBenchmark();

		return runned;
	}

	public boolean count(RunSpecification runSpecification) {
		BenchmarkRunSetup benchmarkRunSetup = runSpecification.getBenchmarkRunSetup();
		BenchmarkRun benchmarkRun = runSpecification.getBenchmarkRun();

		if (benchmarkRunSetup.isValidationRequired()) {
			try {
				VertexCounter counter = new VertexCounter(benchmarkRunSetup.getOutputDir());
				long expected = benchmarkRun.getGraph().getNumberOfVertices();
				long parsed = counter.count();
				if(parsed != expected) {
					return false;
				}
			} catch (ValidatorException e) {
				LOG.error("Failed to count the number of outputs: " + e);
				return false;
			}
		}
		return true;
	}

	public boolean validate(RunSpecification runSpecification) {
		BenchmarkRunSetup benchmarkRunSetup = runSpecification.getBenchmarkRunSetup();
		BenchmarkRun benchmarkRun = runSpecification.getBenchmarkRun();

		boolean validated = true;

		if (benchmarkRunSetup.isValidationRequired()) {
			ValidationRule validationRule = benchmarkRun.getAlgorithm().getValidationRule();

			@SuppressWarnings("rawtypes")
			VertexValidator<?> validator;
			if(validationRule instanceof EpsilonValidationRule) {
				validator = new DoubleVertexValidator(benchmarkRunSetup.getOutputDir(),
						benchmarkRunSetup.getValidationDir(),
						validationRule, true);
			} else {
				validator = new LongVertexValidator(benchmarkRunSetup.getOutputDir(),
						benchmarkRunSetup.getValidationDir(),
						validationRule, true);
			}

			try {
				validated = validator.validate();
			} catch (ValidatorException e) {
				LOG.error("Failed to validate output: " + e);
				validated = false;
			}
		}

		benchmarkStatus.setValidated(validated);
		return validated;
	}

	public BenchmarkRunResult summarize(BenchmarkRun benchmarkRun, BenchmarkMetrics metrics) {

		// calculate makespan
		long makespanMS = (benchmarkStatus.getEndOfBenchmark().getTime() - benchmarkStatus.getStartOfBenchmark().getTime());
		BigDecimal makespanS = (new BigDecimal(makespanMS)).divide(new BigDecimal(1000), 3, BigDecimal.ROUND_CEILING);
		metrics.setMakespan(new BenchmarkMetric(makespanS, "s"));

		BenchmarkRunResult benchmarkRunResult =
				new BenchmarkRunResult(benchmarkRun, benchmarkStatus, new BenchmarkFailures(), metrics);

		return benchmarkRunResult;
	}

	/**
	 * Terminate benchmark runner process.
	 */
	public static void terminateRunner(BenchmarkRunStatus runnerInfo) {

		LOG = LogManager.getLogger();
		LOG.debug(String.format("Terminating benchmark runner."));
		Process process = runnerInfo.getProcess();
		int port = RunnerService.getRunnerPort();

		// Check if the runner process is registered in the benchmark run log.
		Integer processId = null;
		try {
			processId = retrieveRunnerProcessId(runnerInfo);
			LOG.debug(String.format("Found runner process id %s", processId));
		} catch (Exception e) {
			LOG.error("Failed to find the process id for the runner process.");
			throw new GraphalyticsExecutionException("Failed to find benchmark runner registration. Benchmark aborted.", e);
		}

		// First attempt to terminate runner process gracefully.
		LOG.debug("Terminating runner process gracefully.");
		ProcessUtil.terminateProcess(process);

		boolean terminated = ProcessUtil.isNetworkPortAvailable(port) && !ProcessUtil.isProcessAlive(processId);

		LOG.debug(String.format("Runner process is %s: (process alive=%s, port available=%s)",
				terminated ? "terminated" : "alive",
				ProcessUtil.isProcessAlive(processId),
				ProcessUtil.isNetworkPortAvailable(port)));

		while (!terminated) {
			LOG.warn("Terminating runner process forcibly.");
			try {
				ProcessUtil.terminateProcess(processId);
			} catch (Exception e) {
				LOG.error("Failed to terminated runner process.", e);
			}

			if(!terminated) {
				LOG.error(String.format("Failed to kill runner process."));
				TimeUtil.waitFor(10);
			}
			terminated = ProcessUtil.isNetworkPortAvailable(port) && !ProcessUtil.isProcessAlive(processId);

			LOG.debug(String.format("Runner process is %s: (process alive=%s, port available=%s)",
					terminated ? "terminated" : "alive",
					ProcessUtil.isProcessAlive(processId),
					ProcessUtil.isNetworkPortAvailable(port)));
		}
	}

	/**
	 * Standard termination method for platform process when time-out occurs.
	 * @param runSpecification
	 */
	public static void terminatePlatform(RunSpecification runSpecification) {

		LOG = LogManager.getLogger();
		LOG.debug(String.format("Terminating platform process(es)."));

		BenchmarkRunSetup benchmarkRunSetup = runSpecification.getBenchmarkRunSetup();

		Path pidFile = benchmarkRunSetup.getLogDir().resolve("platform").resolve("executable.pid");
		if(pidFile.toFile().exists()) {

			Integer processId = null;
			try {
				processId =  Integer.parseInt((new String(readAllBytes(pidFile))).trim());
				LOG.debug(String.format("Found platform process id " + processId));
			} catch (Exception e) {
				LOG.error(String.format("Failed to parse process id from executable.pid file.", e));
				return;
			}

			boolean terminated = !ProcessUtil.isProcessAlive(processId);
			LOG.debug(String.format("Platform process %s is %s.", processId, terminated ? "terminated" : "alive"));

			while(!terminated) {
				try {
					LOG.warn(String.format("Terminating platform process forcibly."));
					ProcessUtil.terminateProcess(processId);
					terminated = !ProcessUtil.isProcessAlive(processId);

					if(!terminated) {
						LOG.error(String.format("Failed to kill platform process."));
						TimeUtil.waitFor(10);
					}
				} catch (Exception e) {
					LOG.error(String.format("Failed to kill platform process."), e);
				}
			}
		} else {
			LOG.error("Failed to find the executable.pid file for platform process.");
		}
	}


	public static void registerRunnerProcessId(Path logDir) throws Exception {
		Path pidFile = logDir.resolve("platform").resolve("runner.pid");
		pidFile.toFile().getParentFile().mkdirs();
		pidFile.toFile().createNewFile();
		Files.write(pidFile, String.valueOf(ProcessUtil.getProcessId()).getBytes());
	}

	public static int retrieveRunnerProcessId(BenchmarkRunStatus runnerInfo) throws Exception {
		Path pidFile = runnerInfo.getRunSpecification().getBenchmarkRunSetup().getLogDir().resolve("platform").resolve("runner.pid");
		String content = new String(readAllBytes(pidFile));
		return Integer.parseInt(content);
	}


	public Platform getPlatform() {
		return platform;
	}

	public void setPlatform(Platform platform) {
		this.platform = platform;
	}

	public String getBenchmarkId() {
		return benchmarkId;
	}

	public void setBenchmarkId(String benchmarkId) {
		this.benchmarkId = benchmarkId;
	}

	public Plugins getPlugins() {
		return plugins;
	}

	public void setPlugins(Plugins plugins) {
		this.plugins = plugins;
	}

	public void setService(RunnerService service) {
		this.service = service;
	}
}
