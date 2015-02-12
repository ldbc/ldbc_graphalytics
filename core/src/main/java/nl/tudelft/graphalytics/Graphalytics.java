package nl.tudelft.graphalytics;

import nl.tudelft.graphalytics.configuration.InvalidConfigurationException;
import nl.tudelft.graphalytics.domain.BenchmarkSuite;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult;
import nl.tudelft.graphalytics.reporting.BenchmarkReport;
import nl.tudelft.graphalytics.reporting.html.HtmlBenchmarkReportGenerator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;

public class Graphalytics {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		// Get the first command-line argument (platform name)
		if (args.length < 1) {
			log.fatal("Missing argument <platform>.");
			System.exit(1);
		}
		String platform = args[0];

		// Use the Reflections library to find the Platform subclass for the given platform
		Reflections reflections = new Reflections("nl.tudelft.graphalytics." + platform);
		Set<Class<? extends Platform>> platformClasses = reflections.getSubTypesOf(Platform.class);
		if (platformClasses.size() == 0) {
			log.fatal("Cannot find a subclass of \"nl.tudelft.graphalytics.Platform\" in package \"" +
					"nl.tudelft.graphalytics." + platform + "\".");
			System.exit(2);
		} else if (platformClasses.size() > 1) {
			log.fatal("Found multiple subclasses of \"nl.tudelft.graphalytics.Platform\"" +
					"in package \"nl.tudelft.graphalytics." + platform + "\".");
			System.exit(3);
		}

		// Attempt to instantiate the Platform subclass to run the benchmark
		Platform platformInstance = null;
		try {
			platformInstance = new ArrayList<>(platformClasses).get(0).newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			log.catching(Level.FATAL, e);
			System.exit(4);
		}

		// Load the benchmark suite from the configuration files
		BenchmarkSuite benchmarkSuite = null;
		try {
			benchmarkSuite = BenchmarkSuiteLoader.readBenchmarkSuiteFromProperties();
		} catch (InvalidConfigurationException | ConfigurationException e) {
			log.fatal("Failed to parse benchmark configuration: ", e);
			System.exit(5);
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
			log.error("Failed to write report: ", e);
			log.info("Attempting to write report to temporary directory.");
			// Attempt to write the benchmark report to a unique temporary directory
			Path tempDirectory = null;
			try {
				tempDirectory = Files.createTempDirectory(platformInstance.getName() + "-report-");
				report.write(tempDirectory.toString());
			} catch (IOException ex) {
				log.fatal("Failed to write report to temporary directory: ", ex);
				System.exit(6);
			}
			log.info("Wrote benchmark report to \"" + tempDirectory.toString() + "\".");
		}
	}

}
