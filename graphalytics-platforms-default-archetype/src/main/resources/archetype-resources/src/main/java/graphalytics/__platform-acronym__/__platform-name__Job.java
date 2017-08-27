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
package science.atlarge.graphalytics.${platform-acronym};

import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.execution.RunSpecification;
import science.atlarge.graphalytics.execution.BenchmarkRunSetup;
import org.apache.commons.exec.util.StringUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.nio.file.Paths;


/**
 * Base class for all jobs in the platform driver. Configures and executes a platform job using the parameters
 * and executable specified by the subclass for a specific algorithm.
 *
 * @author ${developer-name}
 */
public abstract class ${platform-name}Job {

	private static final Logger LOG = LogManager.getLogger();

	protected CommandLine commandLine;
    private final String jobId;
	private final String logPath;
	private final String inputPath;
	private final String outputPath;

	protected final ${platform-name}Configuration platformConfig;

	/**
     * Initializes the platform job with its parameters.
	 * @param runSpecification the benchmark run specification.
	 * @param platformConfig the platform configuration.
	 * @param inputPath the file path of the input graph dataset.
	 * @param outputPath the file path of the output graph dataset.
	 */
	public ${platform-name}Job(RunSpecification runSpecification, ${platform-name}Configuration platformConfig,
		String inputPath, String outputPath) {

		BenchmarkRun benchmarkRun = runSpecification.getBenchmarkRun();
		BenchmarkRunSetup benchmarkRunSetup = runSpecification.getBenchmarkRunSetup();

		this.jobId = benchmarkRun.getId();
		this.logPath = benchmarkRunSetup.getLogDir().resolve("platform").toString();

		this.inputPath = inputPath;
		this.outputPath = outputPath;

		this.platformConfig = platformConfig;
	}


	/**
	 * Executes the platform job with the pre-defined parameters.
	 *
	 * @return the exit code
	 * @throws IOException if the platform failed to run
	 */
	public int execute() throws Exception {
		String executableDir = platformConfig.getExecutablePath();
		commandLine = new CommandLine(Paths.get(executableDir).toFile());

		// List of benchmark parameters.
		String jobId = getJobId();
		String logDir = getLogPath();

		// List of dataset parameters.
		String inputPath = getInputPath();
		String outputPath = getOutputPath();

		// List of platform parameters.
		int numMachines = platformConfig.getNumMachines();
		int numThreads = platformConfig.getNumThreads();
		String homeDir = platformConfig.getHomePath();

		appendBenchmarkParameters(jobId, logDir);
		appendAlgorithmParameters();
		appendDatasetParameters(inputPath, outputPath);
		appendPlatformConfigurations(homeDir, numMachines, numThreads);

		String commandString = StringUtils.toString(commandLine.toStrings(), " ");
		LOG.info(String.format("Execute benchmark job with command-line: [%s]", commandString));

		Executor executor = new DefaultExecutor();
		executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
		executor.setExitValue(0);
		return executor.execute(commandLine);
	}


	/**
	 * Appends the benchmark-specific parameters for the executable to a CommandLine object.
	 */
	private void appendBenchmarkParameters(String jobId, String logPath) {

		commandLine.addArgument("--job-id");
		commandLine.addArgument(jobId);

		commandLine.addArgument("--log-path");
		commandLine.addArgument(logPath);

	}

	/**
	 * Appends the dataset-specific parameters for the executable to a CommandLine object.
	 */
	private void appendDatasetParameters(String inputPath, String outputPath) {

		commandLine.addArgument("--input-path");
		commandLine.addArgument(Paths.get(inputPath).toString());

		commandLine.addArgument("--output-path");
		commandLine.addArgument(Paths.get(outputPath).toString());

	}


	/**
	 * Appends the platform-specific parameters for the executable to a CommandLine object.
	 */
	private void appendPlatformConfigurations(String homeDir, int numMachines, int numThreads) {

		if(homeDir != null && !homeDir.trim().isEmpty()) {
			commandLine.addArgument("--home-dir");
			commandLine.addArgument(homeDir);
		}

		commandLine.addArgument("--num-machines");
		commandLine.addArgument(String.valueOf(numMachines));

		commandLine.addArgument("--num-threads");
		commandLine.addArgument(String.valueOf(numThreads));

	}


	/**
	 * Appends the algorithm-specific parameters for the executable to a CommandLine object.
	 */
	protected abstract void appendAlgorithmParameters();

	private String getJobId() {
		return jobId;
	}

	public String getLogPath() {
		return logPath;
	}

	private String getInputPath() {
		return inputPath;
	}

	private String getOutputPath() {
		return outputPath;
	}

}
