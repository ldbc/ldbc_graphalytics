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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Container for BenchmarkReportFiles that describe the results of a benchmark suite execution. This class defines a
 * write method that allows the full benchmark report to be written to an arbitrary path.
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class BenchmarkReport {

	private final String reportTypeIdentifier;
	private final Collection<BenchmarkReportFile> files;

	/**
	 * @param reportTypeIdentifier an identifier for the output format (e.g. "html" or "csv")
	 * @param files                a collection of files that define the contents of the benchmark report
	 */
	public BenchmarkReport(String reportTypeIdentifier, Collection<? extends BenchmarkReportFile> files) {
		this.reportTypeIdentifier = reportTypeIdentifier;
		this.files = new ArrayList<>(files);
	}

	/**
	 * @param path a directory to write the report to, must be non-existent or an empty directory
	 * @throws IOException if an exception occurred during writing, or if path already exists
	 */
	public void write(Path path) throws IOException {
		// Ensure that the path does not yet exist, and create it, or that it is a directory
		if (Files.exists(path)) {
			if (!Files.isDirectory(path))
				throw new IOException("Output path of report already exists: \"" + path + "\".");
		} else {
			Files.createDirectory(path);
		}

		// Write the individual files
		for (BenchmarkReportFile benchmarkReportFile : files) {
			benchmarkReportFile.write(path);
		}
	}

	/**
	 * @return an identifier for the output format (e.g. "html" or "csv")
	 */
	public String getReportTypeIdentifier() {
		return reportTypeIdentifier;
	}

}
