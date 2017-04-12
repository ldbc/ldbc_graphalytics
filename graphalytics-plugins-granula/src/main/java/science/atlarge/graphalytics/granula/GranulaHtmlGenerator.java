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
package science.atlarge.graphalytics.granula;

import science.atlarge.graphalytics.report.result.BenchmarkResult;
import science.atlarge.graphalytics.report.result.BenchmarkSuiteResult;
import science.atlarge.graphalytics.report.html.HtmlBenchmarkReportGenerator;

/**
 * @author Tim Hegeman
 */
public class GranulaHtmlGenerator implements HtmlBenchmarkReportGenerator.Plugin {


	@Override
	public void preGenerate(HtmlBenchmarkReportGenerator htmlBenchmarkReportGenerator, BenchmarkSuiteResult result) {
		for (BenchmarkResult benchmarkResult : result.getBenchmarkResults()) {
			if (benchmarkResult.isSuccessful()) {
				htmlBenchmarkReportGenerator.registerPageLink(benchmarkResult.getBenchmarkRun().getId(),
						String.format("archive/%s/visualizer.htm", benchmarkResult.getBenchmarkRun().getId()));
			}
		}
	}


}
