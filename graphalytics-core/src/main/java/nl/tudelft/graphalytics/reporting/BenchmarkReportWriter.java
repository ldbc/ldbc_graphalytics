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
package nl.tudelft.graphalytics.reporting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Utility class for writing a benchmark report to disk at a standardized location.
 *
 * @author Tim Hegeman
 */
public class BenchmarkReportWriter {
	private static final Logger LOG = LogManager.getLogger();

	private static final String DATA_SUBDIR = "data";

	private final String platformName;
	private boolean outputDirectoryCreated;
	private Path outputDirectoryPath;

	public BenchmarkReportWriter(String platformName) {
		this.platformName = platformName;
		this.outputDirectoryCreated = false;
		this.outputDirectoryPath = null;
	}

	/**
	 * Creates the output directory to which the benchmark report will be written. The name of the output directory
	 * contains a timestamp determined at the moment this method is first called. This method may be used before running
	 * the benchmark to have the report timestamp coincide with the start of the benchmark.
	 *
	 * @throws IOException
	 */
	public void createOutputDirectory() throws IOException {
		if (outputDirectoryCreated) {
			return;
		}

		String timestamp = new SimpleDateFormat("yyMMdd-HHmmss").format(Calendar.getInstance().getTime());
		int attempt = 0;
		while (!outputDirectoryCreated) {
			outputDirectoryPath = formatOuptutDirectoryForAttempt(timestamp, attempt);
			outputDirectoryCreated = attemptCreateOutputDirectory();
			attempt++;
		}
	}

	/**
	 * Formats a directory name that will be used to create the output directory for the benchmark report. The directory
	 * name includes the name of the platform, the timestamp at which the benchmark was started, and (optionally) a
	 * sequence number in case of conflicts.
	 *
	 * @param timestamp the timestamp at which the benchmark was started
	 * @param attempt the number of attempts to create the output directory that have already failed
	 * @return the name of the output directory to attempt next
	 */
	private Path formatOuptutDirectoryForAttempt(String timestamp, int attempt) {
		String base = "report/" + "report-" + platformName + "-" + timestamp;
		if (attempt == 0) {
			return Paths.get(base);
		} else {
			return Paths.get(base + "-" + attempt);
		}
	}

	/**
	 * Attempt to create the output directory located at {@code outputDirectoryPath}.
	 *
	 * @return true if the directory was created, false if the directory already exists
	 * @throws IOException if an error occurred during creation of the directory
	 */
	private boolean attemptCreateOutputDirectory() throws IOException {
		try {
			Files.createDirectories(outputDirectoryPath);
			return true;
		} catch (IOException ex) {
			// Return false if the directory already exists, throw otherwise
			if (Files.exists(outputDirectoryPath)) {
				return false;
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
			report.write(reportPath);
			LOG.info("Wrote benchmark report of type \"{}\" to \"{}\".", report.getReportTypeIdentifier(), reportPath);
		} catch (IOException e) {
			LOG.error("Failed to write report: ", e);
		}
	}

	public Path getOrCreateOutputDirectoryPath() {
		return outputDirectoryPath;
	}

	public Path getOrCreateOutputDataPath() throws IOException {
		return attemptCreateSubdirectory(DATA_SUBDIR);
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
