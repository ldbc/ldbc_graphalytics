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

import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult;
import nl.tudelft.graphalytics.reporting.BenchmarkReport;
import nl.tudelft.graphalytics.reporting.BenchmarkReportData;
import nl.tudelft.graphalytics.reporting.BenchmarkReportFile;
import nl.tudelft.graphalytics.reporting.BenchmarkReportGenerator;

import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Utility class for generating an HTML-based BenchmarkReport from a BenchmarkSuiteResult.
 *
 * @author Tim Hegeman
 */
public class HtmlBenchmarkReportGenerator implements BenchmarkReportGenerator {

	public static final String REPORT_TYPE_IDENTIFIER = "html";

	private static final String INDEX_HTML = "index";
	private static final String[] STATIC_RESOURCES = new String[]{
			// Bootstrap CSS and JS
			"lib/bootstrap/css/bootstrap.min.css",
			"lib/bootstrap/css/bootstrap-theme.min.css",
			"lib/bootstrap/fonts/glyphicons-halflings-regular.eot",
			"lib/bootstrap/fonts/glyphicons-halflings-regular.svg",
			"lib/bootstrap/fonts/glyphicons-halflings-regular.ttf",
			"lib/bootstrap/fonts/glyphicons-halflings-regular.woff",
			"lib/bootstrap/js/bootstrap.min.js",
			"lib/bootstrap/js/jquery.js",
			// Report CSS
			"lib/graphalytics/css/carousel.css",
			"lib/graphalytics/css/report.css"
	};

	@Override
	public BenchmarkReport generateReportFromResults(BenchmarkSuiteResult result) {
		// Initialize the template engine
		TemplateEngine templateEngine = new TemplateEngine();
		templateEngine.putVariable("report", new BenchmarkReportData(result));
		templateEngine.putVariable("util", new TemplateUtility());

		// Generate the report files
		Collection<BenchmarkReportFile> reportFiles = new LinkedList<>();
		// 1. Generate the index page
		String indexHtml = templateEngine.processTemplate(INDEX_HTML);
		reportFiles.add(new GeneratedHtmlPage(indexHtml, ".", INDEX_HTML));
		// 2. Copy the static resources
		for (String resource : STATIC_RESOURCES) {
			URL resourceUrl = HtmlBenchmarkReportGenerator.class.getResource("/graphalytics/reporting/html/" + resource);
			reportFiles.add(new StaticResource(resourceUrl, resource));
		}

		return new BenchmarkReport(REPORT_TYPE_IDENTIFIER, reportFiles);
	}

}
