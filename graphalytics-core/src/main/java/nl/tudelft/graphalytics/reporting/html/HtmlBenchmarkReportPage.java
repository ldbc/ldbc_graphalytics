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
package nl.tudelft.graphalytics.reporting.html;

import nl.tudelft.graphalytics.reporting.BenchmarkReportPage;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A single page in the HTML benchmark report generated for a given execution of the Graphalytics benchmark suite.
 *
 * @author Tim Hegeman
 */
public class HtmlBenchmarkReportPage implements BenchmarkReportPage {

	private String htmlData;
	private String relativePath;
	private String baseFilename;

	/**
	 * @param htmlData the raw HTML data for this report page
	 * @param relativePath the path relative to the report root to write this page to, or "." for root
	 * @param baseFilename the filename (excluding extension) of this page
	 */
	public HtmlBenchmarkReportPage(String htmlData, String relativePath, String baseFilename) {
		this.htmlData = htmlData;
		this.relativePath = relativePath;
		this.baseFilename = baseFilename;
	}

	@Override
	public void write(String reportPath) throws IOException {
		// Ensure that the output directory exists
		Path pagePath = Paths.get(reportPath, relativePath);
		if (!Files.exists(pagePath))
			Files.createDirectory(pagePath);
		if (!Files.isDirectory(pagePath))
			throw new IOException("Path \"" + pagePath + "\" exists, but is not a directory.");

		// Write the HTML data to a file
		File pageFile = pagePath.resolve(baseFilename + ".html").toFile();
		FileUtils.writeStringToFile(pageFile, htmlData);
	}

}
