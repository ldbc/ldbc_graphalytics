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
package nl.tudelft.graphalytics.granula;

import nl.tudelft.graphalytics.domain.BenchmarkResult;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult;
import nl.tudelft.graphalytics.granula.logging.GangliaLogger;
import nl.tudelft.graphalytics.granula.logging.UtilizationLogger;
import nl.tudelft.pds.granula.GranulaArchiver;
import nl.tudelft.pds.granula.archiver.source.JobDirectorySource;
import nl.tudelft.pds.granula.modeller.model.job.JobModel;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by wlngai on 10-9-15.
 */
public class GranulaManager {

	private static final Logger LOG = LogManager.getLogger();

	/**
	 * Property key for enabling or disabling Granula.
	 */
	private static final String GRANULA_ENABLED = "benchmark.run.granula.enabled";
	private static final String LOGGING_ENABLED = "benchmark.run.granula.logging-enabled";
	private static final String LOGGING_PRESERVED = "benchmark.run.granula.logging-preserved";
	private static final String ARCHIVING_ENABLED = "benchmark.run.granula.archiving-enabled";

	private static final String UTILIZATION_LOGGING_ENABLED = "benchmark.run.granula.utilization-logging-enabled";
	private static final String UTILIZATION_LOGGING_TOOL = "benchmark.run.granula.utilization-logging-tool";

	public static boolean isGranulaEnabled;
	public static boolean isLoggingEnabled;
	public static boolean isLogDataPreserved;
	public static boolean isArchivingEnabled;
	public static boolean isUtilLoggingEnabled;

	public static UtilizationLogger utilizationLogger;

	JobModel model;
	Path reportDataPath;

	public GranulaManager(GranulaAwarePlatform platform) {
		// Load Granula configuration
		PropertiesConfiguration granulaConfig;
		try {
			granulaConfig = new PropertiesConfiguration("granula.properties");
			isGranulaEnabled = granulaConfig.getBoolean(GRANULA_ENABLED, false);
			isLoggingEnabled = granulaConfig.getBoolean(LOGGING_ENABLED, false);
			isLogDataPreserved = granulaConfig.getBoolean(LOGGING_PRESERVED, false);
			isArchivingEnabled = granulaConfig.getBoolean(ARCHIVING_ENABLED, false);
			isUtilLoggingEnabled = granulaConfig.getBoolean(UTILIZATION_LOGGING_ENABLED, false);

			if(isGranulaEnabled) {
				LOG.info("Granula plugin is found, and is enabled.");
				LOG.info(String.format(" - Logging is %s for Granula.", (isLoggingEnabled) ? "enabled" : "disabled"));
				LOG.info(String.format(" - Archiving is %s for Granula.", (isArchivingEnabled) ? "enabled" : "disabled"));
				LOG.info(String.format(" - Logging data is %s after being used by Granula.", (isLogDataPreserved) ? "preserved" : "not preserved"));
			} else {
				LOG.info("Granula plugin is found, but is disabled.");
			}

			if (isArchivingEnabled && !isLoggingEnabled) {
				LOG.error(String.format("The archiving feature (%s) is not usable while logging feature (%s) is not enabled. " +
						"Turning off the archiving feature of Granula. ", ARCHIVING_ENABLED, LOGGING_ENABLED));
				isGranulaEnabled = false;
			}

			String utilToolName = granulaConfig.getString(UTILIZATION_LOGGING_TOOL);

			switch (utilToolName) {
				case "ganglia":
					utilizationLogger = new GangliaLogger();
					break;
				default:
					throw new IllegalArgumentException(String.format("%s is a valid utilization logging tool", utilToolName));
			}

		} catch (ConfigurationException e) {
			LOG.info("Could not find or load granula.properties.");
		}
		setModel(platform.getGranulaModel());
	}

	public void archive(String inputPath, String outputPath) {
		JobDirectorySource jobDirSource = new JobDirectorySource(inputPath);
		jobDirSource.load();

		GranulaArchiver granulaArchiver = new GranulaArchiver(jobDirSource, model, outputPath);
		granulaArchiver.archive();
	}

	public void generateArchive(BenchmarkSuiteResult benchmarkSuiteResult) throws IOException {
		// Ensure the log and archive directories exist
		Path logPath = reportDataPath.resolve("log");
		Path archivePath = reportDataPath.resolve("archive");
		Files.createDirectories(logPath);
		Files.createDirectories(archivePath);

		for (BenchmarkResult benchmarkResult : benchmarkSuiteResult.getBenchmarkResults()) {
			// make sure the log path(s) exists.
			Path benchmarkLogPath = logPath.resolve(benchmarkResult.getBenchmark().getBenchmarkIdentificationString());
			Files.createDirectories(benchmarkLogPath.resolve("OperationLog"));
			Files.createDirectories(benchmarkLogPath.resolve("UtilizationLog"));

			// make sure the archive path exists.
			Path archiveFile = archivePath.resolve(benchmarkResult.getBenchmark().getBenchmarkIdentificationString() + ".xml");

			// archive
			archive(benchmarkLogPath.toString(), archiveFile.toString());
		}
	}

	public void setModel(JobModel model) {
		this.model = model;
	}

	public void setReportDirPath(Path reportDataPath) {
		this.reportDataPath = reportDataPath;
	}

}
