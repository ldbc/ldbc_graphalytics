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
package nl.tudelft.graphalytics;

import nl.tudelft.graphalytics.execution.BenchmarkLoader;
import nl.tudelft.graphalytics.configuration.InvalidConfigurationException;
import nl.tudelft.graphalytics.configuration.PlatformParser;
import nl.tudelft.graphalytics.domain.benchmark.Benchmark;
import nl.tudelft.graphalytics.execution.BenchmarkSuiteExecutor;
import nl.tudelft.graphalytics.report.result.BenchmarkSuiteResult;
import nl.tudelft.graphalytics.plugin.Plugins;
import nl.tudelft.graphalytics.report.BenchmarkReport;
import nl.tudelft.graphalytics.report.BenchmarkReportWriter;
import nl.tudelft.graphalytics.report.html.HtmlBenchmarkReportGenerator;
import nl.tudelft.graphalytics.util.LogUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Graphalytics {

	private static final Logger LOG = LogManager.getLogger();

	public static void main(String[] args) throws IOException {
		Platform platform;
		BenchmarkLoader benchmarkLoader;
		BenchmarkReportWriter reportWriter;
		BenchmarkSuiteExecutor benchmarkSuiteExecutor;

		// Get an instance of the platform integration code
		platform = PlatformParser.loadPlatformFromCommandLineArgs(args);

		LogUtil.logBenchmarkHeader(platform.getPlatformName());
		// Load the benchmark suite from the configuration files
		// Prepare the benchmark report directory for writing
		reportWriter = new BenchmarkReportWriter(platform.getPlatformName());
		reportWriter.createOutputDirectory();

		// load benchmark from configuration.
		Benchmark benchmark;
		try {
			benchmarkLoader = new BenchmarkLoader(reportWriter);
			benchmark = benchmarkLoader.parse();
		} catch (InvalidConfigurationException e) {
			throw new GraphalyticsLoaderException("Failed to parse benchmark configuration.", e);
		}

		// Initialize any loaded plugins
		Plugins plugins = Plugins.discoverPluginsOnClasspath(platform, benchmark, reportWriter);
		// Signal to all plugins the start of the benchmark suite
		plugins.preBenchmarkSuite(benchmark);
		// Run the benchmark
		benchmarkSuiteExecutor = new BenchmarkSuiteExecutor(benchmark, platform, plugins);
		BenchmarkSuiteResult benchmarkSuiteResult = benchmarkSuiteExecutor.execute();
		// Notify all plugins of the result of running the benchmark suite
		plugins.postBenchmarkSuite(benchmark, benchmarkSuiteResult);

		// Generate the benchmark report
		HtmlBenchmarkReportGenerator htmlBenchmarkReportGenerator = new HtmlBenchmarkReportGenerator();
		plugins.preReportGeneration(htmlBenchmarkReportGenerator);
		BenchmarkReport report = htmlBenchmarkReportGenerator.generateReportFromResults(benchmarkSuiteResult);
		// Write the benchmark report
		reportWriter.writeReport(report);

		// Finalize any loaded plugins
		plugins.shutdown();
	}

}
