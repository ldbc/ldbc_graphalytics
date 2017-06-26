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
package science.atlarge.graphalytics.granula;

import science.atlarge.graphalytics.report.result.BenchmarkRunResult;
import science.atlarge.graphalytics.report.result.BenchmarkResult;
import science.atlarge.graphalytics.report.html.HtmlBenchmarkReportGenerator;

/**
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class GranulaHtmlGenerator implements HtmlBenchmarkReportGenerator.Plugin {


	@Override
	public void preGenerate(HtmlBenchmarkReportGenerator htmlBenchmarkReportGenerator, BenchmarkResult result) {
		for (BenchmarkRunResult benchmarkRunResult : result.getBenchmarkRunResults()) {
			if (benchmarkRunResult.isSuccessful()) {
				htmlBenchmarkReportGenerator.registerPageLink(benchmarkRunResult.getBenchmarkRun().getId(),
						String.format("archive/%s/visualizer.htm", benchmarkRunResult.getBenchmarkRun().getId()));
			}
		}
	}


}
