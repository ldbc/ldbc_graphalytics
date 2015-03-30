package nl.tudelft.graphalytics;

import nl.tudelft.graphalytics.configuration.InvalidConfigurationException;
import nl.tudelft.graphalytics.domain.BenchmarkSuite;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult;
import nl.tudelft.graphalytics.reporting.BenchmarkReport;
import nl.tudelft.graphalytics.reporting.html.HtmlBenchmarkReportGenerator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

		// Run the benchmark
		BenchmarkSuiteResult benchmarkSuiteResult =
				new BenchmarkSuiteRunner(benchmarkSuite, platformInstance).execute();

		// Generate the report
		BenchmarkReport report = HtmlBenchmarkReportGenerator.generateFromBenchmarkSuiteResult(
				benchmarkSuiteResult, "report-template");
		// Attempt to write the benchmark report
		try {
			report.write(platformInstance.getName() + "-report");
		} catch (IOException e) {
			LOG.error("Failed to write report: ", e);
			LOG.info("Attempting to write report to temporary directory.");
			// Attempt to write the benchmark report to a unique temporary directory
			Path tempDirectory;
			try {
				tempDirectory = Files.createTempDirectory(platformInstance.getName() + "-report-");
				report.write(tempDirectory.toString());
			} catch (IOException ex) {
				throw new IOException("Failed to write report to temporary directory: ", ex);
			}
			LOG.info("Wrote benchmark report to \"" + tempDirectory.toString() + "\".");
		}
	}

}
