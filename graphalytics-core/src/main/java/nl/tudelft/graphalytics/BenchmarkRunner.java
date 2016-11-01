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

import nl.tudelft.graphalytics.configuration.PlatformParser;
import nl.tudelft.graphalytics.domain.Benchmark;
import nl.tudelft.graphalytics.domain.BenchmarkResult;
import nl.tudelft.graphalytics.domain.NestedConfiguration;
import nl.tudelft.graphalytics.domain.PlatformBenchmarkResult;
import nl.tudelft.graphalytics.network.RunnerService;
import nl.tudelft.graphalytics.validation.ValidatorException;
import nl.tudelft.graphalytics.validation.VertexValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class BenchmarkRunner {

	private static final Logger LOG = LogManager.getLogger();

	private RunnerService service;

	Platform platform;
	String benchmarkId;

	public static void main(String[] args) throws IOException {
		// Get an instance of the platform integration code
		BenchmarkRunner executor = new BenchmarkRunner();
		String[] args1 = {"reference", "b792084"};
		args1 =args;
		executor.platform = PlatformParser.loadPlatformFromCommandLineArgs(args1);
		executor.benchmarkId = args1[1];
		RunnerService.InitService(executor);
	}

	public static Process InitializeJvmProcess(String platform, String benchmarkId) {
		try {

			String jvm = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			String classpath = System.getProperty("java.class.path");

			String mainClass = BenchmarkRunner.class.getCanonicalName();

			List<String> command = new ArrayList<>();
			command.add(jvm);
			command.add(mainClass);
			command.addAll(Arrays.asList(platform, benchmarkId));
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.redirectErrorStream(true);
			Map<String, String> environment = processBuilder.environment();
			environment.put("CLASSPATH", classpath);


			Process process = processBuilder.start();
//			report(process);
			return process;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}


	public static void TerminateJvmProcess(Process process) {
		process.destroy();
	}

	private static void report(Process process)  {
		InputStream is = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;

		try {
			while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	public BenchmarkResult execute(Benchmark benchmark) {

		Platform platform = getPlatform();


		// Use a BenchmarkResultBuilder to create the BenchmarkResult for this Benchmark
		BenchmarkResult.BenchmarkResultBuilder benchmarkResultBuilder = new BenchmarkResult.BenchmarkResultBuilder(benchmark);

		// Start the timer
		benchmarkResultBuilder.markStartOfBenchmark();

		// Execute the benchmark and collect the result
		PlatformBenchmarkResult platformBenchmarkResult =
				new PlatformBenchmarkResult(NestedConfiguration.empty());
		boolean completed = true;
		boolean validated = true;
		boolean successful = true;

		try {
			platformBenchmarkResult = platform.executeAlgorithmOnGraph(benchmark);
			completed = true;
		} catch(Exception ex) {
			LOG.error("Algorithm \"" + benchmark.getAlgorithm().getName() + "\" on graph \"" +
					benchmark.getGraph().getGraphSet().getName() + " failed to complete:", ex);
		}

		// Stop the timer
		benchmarkResultBuilder.markEndOfBenchmark();

		if (completed && benchmark.isValidationRequired()) {
			validated = validateBenchmark(benchmark);
		}
		successful = benchmark.isValidationRequired() ? completed && validated : completed;

		benchmarkResultBuilder.setCompleted(completed);
		benchmarkResultBuilder.setValidated(validated);
		benchmarkResultBuilder.setSuccessful(successful);

		// Construct the BenchmarkResult and register it
		BenchmarkResult benchmarkResult = benchmarkResultBuilder.buildFromResult(platformBenchmarkResult);
		return benchmarkResult;
	}


	public boolean validateBenchmark(Benchmark benchmark) {

		boolean isValidated = true;

		@SuppressWarnings("rawtypes")
		VertexValidator<?> validator = new VertexValidator(
				Paths.get(benchmark.getOutputPath()),
				Paths.get(benchmark.getValidationPath()),
				benchmark.getAlgorithm().getValidationRule(),
				true);

		try {
			if (!validator.execute()) {
				isValidated = false;
			}
		} catch(ValidatorException e) {
			LOG.error("Failed to validate output: " + e.getMessage());
			isValidated = false;
		}
		return isValidated;
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
