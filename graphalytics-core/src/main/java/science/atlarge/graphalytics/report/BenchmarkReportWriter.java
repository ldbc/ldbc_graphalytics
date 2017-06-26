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
package science.atlarge.graphalytics.report;

import science.atlarge.graphalytics.domain.benchmark.Benchmark;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for writing a benchmark report to disk at a standardized location.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class BenchmarkReportWriter {

	private static final Logger LOG = LogManager.getLogger();
	private static final String DATA_SUBDIR = "data";
	private Path outputDirectoryPath;
	private Benchmark benchmark;

	public BenchmarkReportWriter(Benchmark benchmark) {
		this.benchmark = benchmark;
		this.outputDirectoryPath = benchmark.getBaseReportDir();
	}

	/**
	 * Creates the output directory to which the benchmark report will be written. The name of the output directory
	 * contains a timestamp determined at the moment this method is first called. This method may be used before running
	 * the benchmark to have the report timestamp coincide with the start of the benchmark.
	 *
	 * @throws IOException
	 */
	public void createOutputDirectory() throws IOException {
		try {
			Files.createDirectories(outputDirectoryPath);
		} catch (IOException ex) {
			// Return false if the directory already exists, throw otherwise
			if(Files.exists(outputDirectoryPath)) {
				throw new IllegalStateException(
						String.format("Benchmark aborted: existing benchmark report detected at %s.", outputDirectoryPath));
			}
			throw ex;
		}
	}


	/**
	 * @param report the benchmark report to write to disk
	 */
	public void writeReport(BenchmarkReport report) {
		// Attempt to write the benchmark report
		try {
			Path reportPath = getOrCreateReportPath(report.getReportTypeIdentifier());
			getOrCreateReportPath("json");
			report.write(outputDirectoryPath);
			LOG.info("Wrote benchmark report of type \"{}\" to \"{}\".", report.getReportTypeIdentifier(), outputDirectoryPath);
		} catch (IOException e) {
			LOG.error("Failed to write report: ", e);
		}
	}

	public Path getOrCreateReportPath() throws IOException {
		return outputDirectoryPath;
	}

	public Path getOrCreateReportPath(String reportTypeIdentifier) throws IOException {
		return attemptCreateSubdirectory(reportTypeIdentifier);
	}

	private Path attemptCreateSubdirectory(String subdirectory) throws IOException {
		Path subdirPath = outputDirectoryPath.resolve(subdirectory);
		if (!subdirPath.toFile().exists()) {
			Files.createDirectory(subdirPath);
		}
		return subdirPath;
	}

}
