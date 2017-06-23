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

import org.apache.logging.log4j.Level;
import science.atlarge.graphalytics.report.result.BenchmarkMetric;
import science.atlarge.graphalytics.util.LogUtil;
import science.atlarge.graphalytics.configuration.PlatformParser;
import science.atlarge.graphalytics.report.result.BenchmarkMetrics;
import science.atlarge.graphalytics.report.result.BenchmarkRunResult;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.validation.ValidatorException;
import science.atlarge.graphalytics.validation.VertexCounter;
import science.atlarge.graphalytics.validation.VertexValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.math.BigDecimal;

/**
 *
 * @author Tim Hegeman
 */
public class BenchmarkRunner {

	private static Logger LOG ;

	private RunnerService service;

	Platform platform;
	String benchmarkId;

	BenchmarkStatus benchmarkStatus;


	public static void main(String[] args) throws IOException {
		// Get an instance of the platform integration code


		LogUtil.intializeLoggers();
		LogUtil.appendSimplifiedConsoleLogger(Level.TRACE);
		LOG = LogManager.getLogger();

		LOG.info("Benchmark runner process started.");
		BenchmarkRunner executor = new BenchmarkRunner();
		executor.platform = PlatformParser.loadPlatformFromCommandLineArgs();
		executor.benchmarkId = args[1];
		RunnerService.InitService(executor);
	}

	public BenchmarkRunner() {
		benchmarkStatus = new BenchmarkStatus();
	}

	public void startup(BenchmarkRun benchmarkRun) {
		platform.startup(benchmarkRun);
	}

	public BenchmarkMetrics finalize(BenchmarkRun benchmarkRun) {
		return platform.finalize(benchmarkRun);
	}


	public boolean run(BenchmarkRun benchmarkRun) {

		boolean runned = false;
		Platform platform = getPlatform();

		LOG.info(String.format("Runner executing benchmark %s.", benchmarkRun.getId()));

		// Start the timer
		benchmarkStatus.setStartOfBenchmark();

		// Execute the benchmark and collect the result
		try {
			runned = platform.run(benchmarkRun);
		} catch(Exception ex) {
			LOG.error("Algorithm \"" + benchmarkRun.getAlgorithm().getName() + "\" on graph \"" +
					benchmarkRun.getFormattedGraph().getGraph().getName() + " failed to complete:", ex);
		}

		// Stop the timer
		benchmarkStatus.setEndOfBenchmark();

		return runned;
	}

	public boolean count(BenchmarkRun benchmarkRun) {

		boolean counted = false;

		if (benchmarkRun.isValidationRequired()) {
			try {
				VertexCounter counter = new VertexCounter(benchmarkRun.getOutputDir());
				long  expected = benchmarkRun.getGraph().getNumberOfVertices();
				long  parsed = counter.count();
				if(parsed == expected) {
					counted = true;
				}
			} catch (ValidatorException e) {
				LOG.error("Failed to count the number of outputs: " + e);
				counted = false;
			}
		} else {
			counted = false;
		}

		return counted;
	}

	public boolean validate(BenchmarkRun benchmarkRun) {

		boolean validated = false;

		if (benchmarkRun.isValidationRequired()) {

			@SuppressWarnings("rawtypes")
			VertexValidator<?> validator = new VertexValidator(benchmarkRun.getOutputDir(),
					benchmarkRun.getValidationDir(),
					benchmarkRun.getAlgorithm().getValidationRule(),
					true);

			try {
				if (validator.validate()) {
					validated = true;
				}
			} catch (ValidatorException e) {
				LOG.error("Failed to validate output: " + e);
				validated = false;
			}
		} else {
			validated = false;
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

	public void setService(RunnerService service) {
		this.service = service;
	}
}
