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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Container for BenchmarkReportPages that describe the results of a benchmark suite execution. This class defines a
 * write method that allows the full benchmark report to be written to an arbitrary path.
 *
 * @author Tim Hegeman.
 */
public class BenchmarkReport {

	private Collection<BenchmarkReportPage> pages;

	/**
	 * @param pages a collection of pages that define the contents of the benchmark report
	 */
	public BenchmarkReport(Collection<? extends BenchmarkReportPage> pages) {
		this.pages = new ArrayList<>(pages);
	}

	/**
	 * @param path a directory to write the report to, must be non-existent
	 * @throws IOException if an exception occurred during writing, or if path already exists
	 */
	public void write(String path) throws IOException {
		// Ensure that the directory does not yet exist, and create it, or that it is exists and is empty
		Path reportPath = Paths.get(path);
		if (Files.exists(reportPath)) {
			if (!Files.isDirectory(reportPath))
				throw new IOException("Output path of report already exists: \"" + path + "\".");
			if (reportPath.toFile().list().length > 0)
				throw new IOException("Output directory of report is non-empty: \"" + path + "\".");
		} else {
			Files.createDirectory(reportPath);
		}

		// Write the individual pages
		for (BenchmarkReportPage benchmarkReportPage : pages) {
			benchmarkReportPage.write(path);
		}
	}

}
