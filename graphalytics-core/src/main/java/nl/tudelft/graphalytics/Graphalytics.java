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

import nl.tudelft.graphalytics.configuration.InvalidConfigurationException;
import nl.tudelft.graphalytics.domain.BenchmarkSuite;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult;
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
		// Get the first command-line argument (platform name)
		if (args.length < 1) {
			throw new GraphalyticsLoaderException("Missing argument <platform>.");
		}
		String platform = args[0];

		// Read the <platform>.platform file that should be in the classpath to determine which class to load
		InputStream platformFileStream = Graphalytics.class.getResourceAsStream("/" + platform + ".platform");
		if (platformFileStream == null) {
			throw new GraphalyticsLoaderException("Missing resource \"" + platform + ".platform\".");
		}

		String platformClassName;
		try (Scanner platformScanner = new Scanner(platformFileStream)) {
			if (!platformScanner.hasNext()) {
				throw new GraphalyticsLoaderException("Expected a single line with a class name in \"" + platform +
						".platform\", got an empty file.");
			}

			platformClassName = platformScanner.next();

			if (platformScanner.hasNext()) {
				throw new GraphalyticsLoaderException("Expected a single line with a class name in \"" + platform +
						".platform\", got multiple words.");
			}
		}

		// Load the class by name
		Class<? extends Platform> platformClass;
		try {
			Class<?> platformClassUncasted = Class.forName(platformClassName);
			if (!Platform.class.isAssignableFrom(platformClassUncasted)) {
				throw new GraphalyticsLoaderException("Expected class \"" + platformClassName +
						"\" to be a subclass of \"nl.tudelft.graphalytics.Platform\".");
			}

			platformClass = platformClassUncasted.asSubclass(Platform.class);
		} catch (ClassNotFoundException e) {
			throw new GraphalyticsLoaderException("Could not find class \"" + platformClassName + "\".", e);
		}

		// Attempt to instantiate the Platform subclass to run the benchmark
		Platform platformInstance;
		try {
			platformInstance = platformClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new GraphalyticsLoaderException("Failed to instantiate platform class \"" +
					platformClassName + "\".", e);
		}

		// Load the benchmark suite from the configuration files
		BenchmarkSuite benchmarkSuite;
		try {
			benchmarkSuite = BenchmarkSuiteLoader.readBenchmarkSuiteFromProperties();
		} catch (InvalidConfigurationException | ConfigurationException e) {
			throw new GraphalyticsLoaderException("Failed to parse benchmark configuration.", e);
		}

		// Create the output directory for the benchmark report with the current time as timestamp
		BenchmarkReportWriter reportWriter = new BenchmarkReportWriter(platformInstance.getName());
		reportWriter.createOutputDirectory();

		// Run the benchmark
		BenchmarkSuiteResult benchmarkSuiteResult =
				new BenchmarkSuiteRunner(benchmarkSuite, platformInstance).execute();

		// Generate the report
		BenchmarkReport report = HtmlBenchmarkReportGenerator.generateFromBenchmarkSuiteResult(
				benchmarkSuiteResult, "report-template");
		// Write the benchmark report
		reportWriter.writeReport(report);
	}
}
