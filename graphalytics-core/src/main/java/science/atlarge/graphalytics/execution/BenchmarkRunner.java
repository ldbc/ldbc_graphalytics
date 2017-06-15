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
import science.atlarge.graphalytics.util.LogUtil;
import science.atlarge.graphalytics.configuration.PlatformParser;
import science.atlarge.graphalytics.report.result.BenchmarkMetrics;
import science.atlarge.graphalytics.report.result.BenchmarkRunResult;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.util.ProcessUtil;
import science.atlarge.graphalytics.validation.ValidatorException;
import science.atlarge.graphalytics.validation.VertexValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;

public class BenchmarkRunner {

	private static Logger LOG ;

	private RunnerService service;

	Platform platform;
	String benchmarkId;

	// Use a BenchmarkResultBuilder to create the BenchmarkRunResult for this Benchmark
	BenchmarkRunResult.BenchmarkResultBuilder benchmarkResultBuilder;


	boolean completed = true;
	boolean validated = true;
	boolean successful = true;

	public static void main(String[] args) throws IOException {
		// Get an instance of the platform integration code


		LogUtil.intializeLoggers();
		LogUtil.appendSimplifiedConsoleLogger(Level.TRACE);
		LOG = LogManager.getLogger();

		LOG.info("Benchmark runner process started.");
		BenchmarkRunner executor = new BenchmarkRunner();
		executor.platform = PlatformParser.loadPlatformFromCommandLineArgs(args);
		executor.benchmarkId = args[1];
		RunnerService.InitService(executor);
	}




	public void preprocess(BenchmarkRun benchmarkRun) {
		platform.startup(benchmarkRun);
	}

	public BenchmarkMetrics postprocess(BenchmarkRun benchmarkRun) {
		return platform.finalize(benchmarkRun);
	}


	public boolean execute(BenchmarkRun benchmarkRun) {

		Platform platform = getPlatform();

		LOG.info(String.format("Runner executing benchmark %s.", benchmarkRun.getId()));
		benchmarkResultBuilder = new BenchmarkRunResult.BenchmarkResultBuilder(benchmarkRun);

		// Start the timer
		benchmarkResultBuilder.markStartOfBenchmark();

		// Execute the benchmark and collect the result
		try {
			completed = platform.run(benchmarkRun);
		} catch(Exception ex) {
			LOG.error("Algorithm \"" + benchmarkRun.getAlgorithm().getName() + "\" on graph \"" +
					benchmarkRun.getFormattedGraph().getGraph().getName() + " failed to complete:", ex);
		}

		// Stop the timer
		benchmarkResultBuilder.markEndOfBenchmark();


		return true;
	}

	public BenchmarkRunResult summarize(BenchmarkRun benchmarkRun, BenchmarkMetrics metrics) {

		successful = benchmarkRun.isValidationRequired() ? completed && validated : completed;
		benchmarkResultBuilder.setCompleted(completed);
		benchmarkResultBuilder.setValidated(validated);
		benchmarkResultBuilder.setSuccessful(successful);
		benchmarkResultBuilder.setBenchmarkMetrics(metrics);

		// Construct the BenchmarkRunResult and register it
		BenchmarkRunResult benchmarkRunResult = benchmarkResultBuilder.buildFromResult();

		// calculate makespan
		if(benchmarkRunResult != null) {
			long makespan = (benchmarkRunResult.getEndOfBenchmark().getTime() - benchmarkRunResult.getStartOfBenchmark().getTime());
			metrics.setMakespan(makespan);
		}

		return benchmarkRunResult;
	}


	public void validate(BenchmarkRun benchmarkRun) {

		if (completed && benchmarkRun.isValidationRequired()) {
			boolean isValidated = true;

			@SuppressWarnings("rawtypes")
			VertexValidator<?> validator = new VertexValidator(benchmarkRun.getOutputDir(),
					benchmarkRun.getValidationDir(),
					benchmarkRun.getAlgorithm().getValidationRule(),
					true);

			try {
				if (!validator.execute()) {
					isValidated = false;
				}
			} catch (ValidatorException e) {
				LOG.error("Failed to validate output: " + e.getMessage());
				isValidated = false;
			}
			validated = isValidated;
		} else {
			validated = false;
		}
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
