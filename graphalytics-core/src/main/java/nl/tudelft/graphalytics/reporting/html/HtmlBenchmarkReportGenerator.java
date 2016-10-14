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
import nl.tudelft.graphalytics.domain.BenchmarkResult;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult;
import nl.tudelft.graphalytics.reporting.BenchmarkReport;
import nl.tudelft.graphalytics.reporting.BenchmarkReportFile;
import nl.tudelft.graphalytics.reporting.BenchmarkReportGenerator;
import nl.tudelft.graphalytics.reporting.json.BenchmarkResultData;
import nl.tudelft.graphalytics.util.json.JsonUtil;

import java.net.URL;
import java.util.Arrays;
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
//			"data/benchmark-results.js",
			"lib/css/visualizer.css",
			"lib/js/result-page.js",
			"lib/js/system-page.js",
			"lib/js/conf-pages.js",
			"lib/js/loader.js",
			"lib/js/utility.js",
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
		BenchmarkResultData benchmarkResultData = generateResult(result);


		String resultData =  "var results = " + JsonUtil.toPrettyJson(benchmarkResultData);
		reportFiles.add(new HtmlResultData(resultData, "data", "benchmark-results"));
		// 2. Copy the static resources
		for (String resource : STATIC_RESOURCES) {
			URL resourceUrl = HtmlBenchmarkReportGenerator.class.getResource("/graphalytics/reporting/html/" + resource);
			reportFiles.add(new StaticResource(resourceUrl, resource));
		}

		return new BenchmarkReport(REPORT_TYPE_IDENTIFIER, reportFiles);
	}

	private BenchmarkResultData generateResult(BenchmarkSuiteResult result) {
		BenchmarkResultData benchmarkResultData = new BenchmarkResultData();

		benchmarkResultData.system.addPlatform("Giraph", "giraph",  "1.4.0", "xyz");
		benchmarkResultData.system.addEnvironment("Das5", "das", "5", "da5.vu.nl");
		benchmarkResultData.system.addMachine("20", "XEON 20.12", "Memory (15)", "Infiniband", "SSD");
		benchmarkResultData.system.addTool("graphalytics-core", "1.4.0", "xyz");

		benchmarkResultData.configuration.addTargetScale("L");
		benchmarkResultData.configuration.addResource("cpu-instance", "1", "false");
		benchmarkResultData.configuration.addResource("cpu-core", "32", "true");

		benchmarkResultData.result.addExperiments("e1342", "bfs", Arrays.asList("j123", "j456"));
		benchmarkResultData.result.addExperiments("e8212", "cdlp", Arrays.asList("j123", "j456"));
		benchmarkResultData.result.addExperiments("e2342", "pr", Arrays.asList("j123", "j456"));

		benchmarkResultData.result.addJobs("j123", "bfs", "DG100", "1", "3", Arrays.asList("r123", "r568"));
		benchmarkResultData.result.addJobs("j456", "bfs", "DG100", "1", "3", Arrays.asList("r356", "r234"));

		benchmarkResultData.result.addRun("r123", "142314123", "true","21343", "252");
		benchmarkResultData.result.addRun("r568", "142314123", "true","21343", "252");
		benchmarkResultData.result.addRun("r356", "142314123", "true","21343", "252");
		benchmarkResultData.result.addRun("r234", "142314123", "true","21343", "252");


		for (BenchmarkResult benchmarkResult : result.getBenchmarkResults()) {
			String id = benchmarkResult.getBenchmark().getId();
			long timestamp = benchmarkResult.getStartOfBenchmark().getTime();
			String success = "unknown";
			long makespan =  benchmarkResult.getEndOfBenchmark().getTime() - benchmarkResult.getStartOfBenchmark().getTime();
			String processingTime = "unknown";
			benchmarkResultData.result.addRun(id, String.valueOf(timestamp), success, String.valueOf(makespan), processingTime);

		}

		return benchmarkResultData;
	}


}
