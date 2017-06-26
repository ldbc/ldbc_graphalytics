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
package science.atlarge.graphalytics;

import org.apache.logging.log4j.Level;
import science.atlarge.graphalytics.configuration.GraphalyticsLoaderException;
import science.atlarge.graphalytics.configuration.BuildInformation;
import science.atlarge.graphalytics.util.LogUtil;
import science.atlarge.graphalytics.execution.BenchmarkLoader;
import science.atlarge.graphalytics.configuration.InvalidConfigurationException;
import science.atlarge.graphalytics.configuration.PlatformParser;
import science.atlarge.graphalytics.domain.benchmark.Benchmark;
import science.atlarge.graphalytics.execution.BenchmarkExecutor;
import science.atlarge.graphalytics.execution.Platform;
import science.atlarge.graphalytics.report.result.BenchmarkResult;
import science.atlarge.graphalytics.plugin.Plugins;
import science.atlarge.graphalytics.report.BenchmarkReport;
import science.atlarge.graphalytics.report.BenchmarkReportWriter;
import science.atlarge.graphalytics.report.html.HtmlBenchmarkReportGenerator;
import science.atlarge.graphalytics.util.ConsoleUtil;
import science.atlarge.graphalytics.util.ProcessUtil;
import science.atlarge.graphalytics.util.TimeUtil;

import java.io.IOException;

/**
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class GraphalyticsBenchmark {

	public static void main(String[] args) throws IOException {

		LogUtil.intializeLoggers();
		LogUtil.appendConsoleLogger(Level.INFO);

		Platform platform;
		BenchmarkLoader benchmarkLoader;
		BenchmarkReportWriter reportWriter;
		BenchmarkExecutor benchmarkExecutor;

		// Get an instance of the platform integration code
		platform = PlatformParser.loadPlatformFromCommandLineArgs();

		// Load the benchmark suite from the configuration files
		// load benchmark from configuration.
		Benchmark benchmark;
		try {
			benchmarkLoader = new BenchmarkLoader(platform.getPlatformName());
			benchmark = benchmarkLoader.parse();

		} catch (InvalidConfigurationException e) {
			throw new GraphalyticsLoaderException("Failed to parse benchmark configuration.", e);
		}

		LogUtil.appendFileLogger(Level.INFO, "file-reduced", benchmark.getBaseReportDir().resolve("log/benchmark.log"));
		LogUtil.appendFileLogger(Level.TRACE, "file-full", benchmark.getBaseReportDir().resolve("log/benchmark-full.log"));
		ConsoleUtil.displayTrademark(platform.getPlatformName());

		ConsoleUtil.displayTextualInformation(BuildInformation.loadCoreBuildInfo());
		ConsoleUtil.displayTextualInformation(BuildInformation.loadPlatformBuildInfo());

		ConsoleUtil.displayTextualInformation(benchmark.toString());
		ConsoleUtil.displayTextualInformation("Benchmark started: " + TimeUtil.epoch2Date(System.currentTimeMillis()) + ".");

		// Prepare the benchmark report directory for writing
		reportWriter = new BenchmarkReportWriter(benchmark);
		reportWriter.createOutputDirectory();


		// Initialize any loaded plugins
		Plugins plugins = Plugins.discoverPluginsOnClasspath(platform, benchmark, reportWriter);
		// Signal to all plugins the start of the benchmark suite
		plugins.preBenchmarkSuite(benchmark);
		// Run the benchmark
		benchmarkExecutor = new BenchmarkExecutor(benchmark, platform, plugins);
		BenchmarkResult benchmarkResult = benchmarkExecutor.execute();
		// Notify all plugins of the result of running the benchmark suite
		plugins.postBenchmarkSuite(benchmark, benchmarkResult);

		// Generate the benchmark report
		HtmlBenchmarkReportGenerator htmlBenchmarkReportGenerator = new HtmlBenchmarkReportGenerator();
		plugins.preReportGeneration(htmlBenchmarkReportGenerator);
		BenchmarkReport report = htmlBenchmarkReportGenerator.generateReportFromResults(benchmarkResult);
		// Write the benchmark report
		reportWriter.writeReport(report);

		// Finalize any loaded plugins
		plugins.shutdown();

		ConsoleUtil.displayTextualInformation("Benchmark ended: " + TimeUtil.epoch2Date(System.currentTimeMillis()) + ".");
		ConsoleUtil.displayTrademark(platform.getPlatformName());
	}

}
