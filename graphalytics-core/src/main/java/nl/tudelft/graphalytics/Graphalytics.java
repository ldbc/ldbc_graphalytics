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
import nl.tudelft.graphalytics.plugin.Plugins;
import nl.tudelft.graphalytics.reporting.BenchmarkReport;
import nl.tudelft.graphalytics.reporting.BenchmarkReportWriter;
import nl.tudelft.graphalytics.reporting.html.HtmlBenchmarkReportGenerator;
import org.apache.commons.configuration.ConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class Graphalytics {

	public static void main(String[] args) throws IOException {
		// Get an instance of the platform integration code
		Platform platformInstance = loadPlatformFromCommandLineArgs(args);
		// Load the benchmark suite from the configuration files
		BenchmarkSuite benchmarkSuite = loadBenchmarkSuite();
		// Prepare the benchmark report directory for writing
		BenchmarkReportWriter reportWriter = new BenchmarkReportWriter(platformInstance.getName());
		reportWriter.createOutputDirectory();
		// Initialize any loaded plugins
		Plugins plugins = Plugins.discoverPluginsOnClasspath(platformInstance, benchmarkSuite, reportWriter);
		// Signal to all plugins the start of the benchmark suite
		plugins.preBenchmarkSuite(benchmarkSuite);
		// Run the benchmark
		BenchmarkSuiteResult benchmarkSuiteResult =
				new BenchmarkSuiteRunner(benchmarkSuite, platformInstance, plugins).execute();
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

	private static BenchmarkSuite loadBenchmarkSuite() {
		try {
			return BenchmarkSuiteLoader.readBenchmarkSuiteFromProperties();
		} catch (InvalidConfigurationException | ConfigurationException e) {
			throw new GraphalyticsLoaderException("Failed to parse benchmark configuration.", e);
		}
	}

	private static Platform loadPlatformFromCommandLineArgs(String[] args) {
		String platformName = getPlatformName(args);
		String platformClassName = getPlatformClassName(platformName);
		Class<? extends Platform> platformClass = getPlatformClassForName(platformClassName);
		return instantiatePlatformClass(platformClass);
	}

	private static String getPlatformName(String[] args) {
		// Get the first command-line argument (platform name)
		if (args.length < 1) {
			throw new GraphalyticsLoaderException("Missing argument <platform>.");
		}
		return args[0];
	}

	private static String getPlatformClassName(String platformName) {
		InputStream platformFileStream = openPlatformFileStream(platformName);
		String platformClassName;
		try (Scanner platformScanner = new Scanner(platformFileStream)) {
			if (!platformScanner.hasNext()) {
				throw new GraphalyticsLoaderException("Expected a single line with a class name in \"" + platformName +
						".platform\", got an empty file.");
			}

			platformClassName = platformScanner.next();

			if (platformScanner.hasNext()) {
				throw new GraphalyticsLoaderException("Expected a single line with a class name in \"" + platformName +
						".platform\", got multiple words.");
			}
		}
		return platformClassName;
	}

	private static InputStream openPlatformFileStream(String platformName) {
		// Read the <platform>.platform file that should be in the classpath to determine which class to load
		InputStream platformFileStream = Graphalytics.class.getResourceAsStream("/" + platformName + ".platform");
		if (platformFileStream == null) {
			throw new GraphalyticsLoaderException("Missing resource \"" + platformName + ".platform\".");
		}
		return platformFileStream;
	}

	private static Class<? extends Platform> getPlatformClassForName(String platformClassName) {
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
		return platformClass;
	}

	private static Platform instantiatePlatformClass(Class<? extends Platform> platformClass) {
		Platform platformInstance;
		try {
			platformInstance = platformClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new GraphalyticsLoaderException("Failed to instantiate platform class \"" +
					platformClass.getSimpleName() + "\".", e);
		}
		return platformInstance;
	}

}
