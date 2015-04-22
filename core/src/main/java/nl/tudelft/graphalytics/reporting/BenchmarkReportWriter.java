/**
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
import java.nio.file.Paths;

/**
 * Utility class for writing a benchmark report to disk at a standardized location.
 *
 * @author Tim Hegeman
 */
public class BenchmarkReportWriter {
	private static final Logger LOG = LogManager.getLogger();

	private final String platformName;
	private boolean outputDirectoryCreated = false;
	private String outputDirectoryPath;

	public BenchmarkReportWriter(String platformName) {
		this.platformName = platformName;
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

		long timestamp = System.currentTimeMillis() / 1000L;
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
	private String formatOuptutDirectoryForAttempt(long timestamp, int attempt) {
		String base = platformName + "-report-" + timestamp;
		if (attempt == 0) {
			return base;
		} else {
			return base + "-" + attempt;
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
			Files.createDirectory(Paths.get(outputDirectoryPath));
			return true;
		} catch (IOException ex) {
			// Return false if the directory already exists, throw otherwise
			if (Files.exists(Paths.get(outputDirectoryPath))) {
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
			createOutputDirectory();
			report.write(outputDirectoryPath);
		} catch (IOException e) {
			LOG.error("Failed to write report: ", e);
		}
		LOG.info("Wrote benchmark report to \"" + outputDirectoryPath + "\".");
	}

}
