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

import nl.tudelft.graphalytics.domain.Benchmark;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult;
import nl.tudelft.graphalytics.reporting.BenchmarkReport;
import nl.tudelft.graphalytics.reporting.BenchmarkReportFile;
import nl.tudelft.graphalytics.reporting.BenchmarkReportGenerator;

import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Utility class for generating an HTML-based BenchmarkReport from a BenchmarkSuiteResult.
 *
 * @author Wing Lung Ngai
 */
public class HtmlBenchmarkReportGenerator implements BenchmarkReportGenerator {

	public static final String REPORT_TYPE_IDENTIFIER = "html";

	private static final String[] STATIC_RESOURCES = new String[]{
			// Bootstrap CSS and JS
			"index.htm",
			"data/src.js",
			"lib/css/management.css",
			"lib/js/tournament-builder.js",
			"lib/js/metric.js",
			"lib/js/selector.js",
			"lib/js/page-loader.js",
			"lib/js/definitions.js",
			"lib/js/pages.js",
			"lib/js/utility.js",
			"lib/js/data.js",
			"lib/external/bootstrap.min.js",
			"lib/external/bootstrap.min.css",
			"lib/external/underscore-min.js",
			"lib/external/bootstrap-table.min.js",
			"lib/external/underscore.string.min.js",
			"lib/external/bootstrap-table.min.css"
	};

	private Map<Benchmark, String> pluginPageLinks;

	@Override
	public BenchmarkReport generateReportFromResults(BenchmarkSuiteResult result) {

		//TODO add plugin code here.

		// Generate the report files
		Collection<BenchmarkReportFile> reportFiles = new LinkedList<>();
		// 1. Generate the resultData
		String resultData = "{}";
		reportFiles.add(new HtmlResultData(resultData, "data", "benchmark-results"));
		// 2. Copy the static resources
		for (String resource : STATIC_RESOURCES) {
			URL resourceUrl = HtmlBenchmarkReportGenerator.class.getResource("/graphalytics/reporting/html/" + resource);
			reportFiles.add(new StaticResource(resourceUrl, resource));
		}

		return new BenchmarkReport(REPORT_TYPE_IDENTIFIER, reportFiles);
	}


}
