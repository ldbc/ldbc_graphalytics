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

import nl.tudelft.graphalytics.configuration.BenchmarkSuiteParser;
import nl.tudelft.graphalytics.configuration.InvalidConfigurationException;
import nl.tudelft.graphalytics.configuration.PlatformParser;
import nl.tudelft.graphalytics.domain.BenchmarkSuite;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult;
import nl.tudelft.graphalytics.plugin.Plugins;
import nl.tudelft.graphalytics.reporting.BenchmarkReport;
import nl.tudelft.graphalytics.reporting.BenchmarkReportWriter;
import nl.tudelft.graphalytics.reporting.html.HtmlBenchmarkReportGenerator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class Graphalytics {

	private static final Logger LOG = LogManager.getLogger();

	public static void main(String[] args) throws IOException {
		// Get an instance of the platform integration code
		Platform platformInstance = PlatformParser.loadPlatformFromCommandLineArgs(args);

		logHeader(platformInstance.getName());
		// Load the benchmark suite from the configuration files
		// Prepare the benchmark report directory for writing
		BenchmarkReportWriter reportWriter = new BenchmarkReportWriter(platformInstance.getName());
		reportWriter.createOutputDirectory();
		BenchmarkSuite benchmarkSuite = loadBenchmarkSuite(reportWriter);

		// Initialize any loaded plugins
		Plugins plugins = Plugins.discoverPluginsOnClasspath(platformInstance, benchmarkSuite, reportWriter);
		// Signal to all plugins the start of the benchmark suite
		plugins.preBenchmarkSuite(benchmarkSuite);
		// Run the benchmark
		BenchmarkSuiteExecutor benchmarkSuiteExecutor = new BenchmarkSuiteExecutor(benchmarkSuite, platformInstance, plugins);
		BenchmarkSuiteResult benchmarkSuiteResult = benchmarkSuiteExecutor.execute();
		// Notify all plugins of the result of running the benchmark suite
		plugins.postBenchmarkSuite(benchmarkSuite, benchmarkSuiteResult);


		// Generate the benchmark report
		HtmlBenchmarkReportGenerator htmlBenchmarkReportGenerator = new HtmlBenchmarkReportGenerator();
		plugins.preReportGeneration(htmlBenchmarkReportGenerator);
		BenchmarkReport report = htmlBenchmarkReportGenerator.generateReportFromResults(benchmarkSuiteResult);
		// Write the benchmark report
		reportWriter.writeReport(report);
		// Finalize any loaded plugins
		plugins.shutdown();
	}

	private static BenchmarkSuite loadBenchmarkSuite(BenchmarkReportWriter reportWriter) {
		try {
			return BenchmarkSuiteParser.readBenchmarkSuiteFromProperties(reportWriter);
		} catch (InvalidConfigurationException | ConfigurationException e) {
			throw new GraphalyticsLoaderException("Failed to parse benchmark configuration.", e);
		}
	}

	private static void logHeader(String name) {

		String seperator = "############################################################";
		String logLine = String.format("###### Running benchmark for %s platform #######", name.toUpperCase());
		LOG.info(seperator.substring(0, logLine.length()));
		LOG.info(seperator.substring(0, logLine.length()));
		LOG.info(logLine);
		LOG.info(seperator.substring(0, logLine.length()));
		LOG.info(seperator.substring(0, logLine.length()));
		LOG.info("");

	}

}
