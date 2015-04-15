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
package nl.tudelft.graphalytics.reporting.html;

import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult;
import nl.tudelft.graphalytics.reporting.BenchmarkReport;
import nl.tudelft.graphalytics.reporting.BenchmarkReportData;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Utility class for generating an HTML-based BenchmarkReport from a BenchmarkSuiteResult.
 *
 * @author Tim Hegeman
 */
public class HtmlBenchmarkReportGenerator {

	private static final String INDEX_HTML = "index";

	private BenchmarkSuiteResult benchmarkSuiteResult;
	private String reportTemplateDir;

	/**
	 * @param benchmarkSuiteResult the results of running a benchmark suite from which a report is to be generated
	 * @param reportTemplateDir    directory containing a template for the benchmark report
	 */
	private HtmlBenchmarkReportGenerator(BenchmarkSuiteResult benchmarkSuiteResult, String reportTemplateDir) {
		this.benchmarkSuiteResult = benchmarkSuiteResult;
		this.reportTemplateDir = reportTemplateDir;
	}

	private BenchmarkReport generate() {
		// Initialize the template engine
		TemplateEngine templateEngine = new TemplateEngine(reportTemplateDir);
		templateEngine.putVariable("report", new BenchmarkReportData(benchmarkSuiteResult));

		// Generate the report pages
		Collection<HtmlBenchmarkReportPage> reportPages = new LinkedList<>();
		// 1. Generate the index page
		String indexHtml = templateEngine.processTemplate(INDEX_HTML);
		reportPages.add(new HtmlBenchmarkReportPage(indexHtml, ".", INDEX_HTML));

		return new HtmlBenchmarkReport(reportPages, reportTemplateDir);
	}

	/**
	 * @param benchmarkSuiteResult the results of running a benchmark suite from which a report is to be generated
	 * @param reportTemplateDir    directory containing a template for the benchmark report
	 * @return the generated benchmark report
	 */
	public static BenchmarkReport generateFromBenchmarkSuiteResult(BenchmarkSuiteResult benchmarkSuiteResult,
	                                                               String reportTemplateDir) {
		return new HtmlBenchmarkReportGenerator(benchmarkSuiteResult, reportTemplateDir).generate();
	}

}
