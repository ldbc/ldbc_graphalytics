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

import science.atlarge.granula.archiver.GranulaExecutor;
import science.atlarge.granula.modeller.entity.Execution;
import science.atlarge.granula.modeller.job.JobModel;
import science.atlarge.granula.modeller.platform.PlatformModel;
import science.atlarge.granula.util.FileUtil;
import science.atlarge.granula.util.json.JsonUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import science.atlarge.graphalytics.configuration.ConfigurationUtil;
import science.atlarge.graphalytics.configuration.GraphalyticsLoaderException;
import science.atlarge.graphalytics.configuration.InvalidConfigurationException;
import science.atlarge.graphalytics.domain.benchmark.Benchmark;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.execution.BenchmarkRunSetup;
import science.atlarge.graphalytics.execution.RunSpecification;
import science.atlarge.graphalytics.execution.BenchmarkFailure;
import science.atlarge.graphalytics.report.result.BenchmarkMetric;
import science.atlarge.graphalytics.report.result.BenchmarkMetrics;
import science.atlarge.graphalytics.report.result.BenchmarkRunResult;
import science.atlarge.graphalytics.report.result.BenchmarkResult;
import science.atlarge.graphalytics.plugin.Plugin;
import science.atlarge.graphalytics.report.BenchmarkReportGenerator;
import science.atlarge.graphalytics.report.BenchmarkReportWriter;
import science.atlarge.graphalytics.report.html.HtmlBenchmarkReportGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class GranulaPlugin implements Plugin {

	private static final Logger LOG = LogManager.getLogger();

	private static final String ARC_DIR = "archive";
	private static final String LOG_DIR = "log";

	private final GranulaAwarePlatform platform;
	private final BenchmarkReportWriter reportWriter;

	private static final String GRANULA_ENABLED = "benchmark.run.granula.enabled";
	private static final String PLATFORM_LOGGING_ENABLED = "benchmark.run.granula.platform-logging";
	private static final String ENVIRONMENT_LOGGING_ENABLED = "benchmark.run.granula.environment-logging";
	private static final String ARCHIVING_ENABLED = "benchmark.run.granula.archiving";

	public static boolean enabled;
	public static boolean platformLogEnabled;
	public static boolean envLogEnabled;
	public static boolean archivingEnabled;

	public GranulaPlugin(GranulaAwarePlatform platform, BenchmarkReportWriter reportWriter) {
		this.platform = platform;
		this.reportWriter = reportWriter;
		loadConfiguration();
	}

	@Override
	public String getPluginName() {
		return "granula";
	}

	@Override
	public String getPluginDescription() {
		return "Granula: Fine Grained Performance Analysis for Big Data Processing Systems";
	}

	@Override
	public void preBenchmarkSuite(Benchmark benchmark) {
		// No operation
	}

	@Override
	public void prepare(RunSpecification runSpecification) {

		BenchmarkRunSetup benchmarkRunSetup = runSpecification.getBenchmarkRunSetup();
		BenchmarkRun benchmarkRun = runSpecification.getBenchmarkRun();


		if(enabled) {
			LOG.debug("Start prepare in Granula");
			if(platformLogEnabled) {
				preserveExecutionLog(platform, benchmarkRun, benchmarkRunSetup.getLogDir());
//				platform.prepare(benchmark, getLogDirectory(benchmarkRun));
			}
		}
	}

	@Override
	public void startup(RunSpecification runSpecification) {

	}

	@Override
	public BenchmarkMetrics finalize(RunSpecification runSpecification, BenchmarkMetrics metrics) {
		return metrics;
	}

	@Override
	public void terminate(RunSpecification runSpecification, BenchmarkRunResult benchmarkRunResult) {
		if (enabled) {
			LOG.debug("Start terminate in Granula");
			if (platformLogEnabled) {
//				platform.terminate(benchmark, getLogDirectory(benchmarkRun));
			}
			if (archivingEnabled) {
				if(benchmarkRunResult !=null && benchmarkRunResult.isSuccessful()) {
					try {
						createArchive(runSpecification, benchmarkRunResult);

						BenchmarkRun benchmarkRun = runSpecification.getBenchmarkRun();

						BenchmarkMetric standardProcTime = benchmarkRunResult.getMetrics().getProcessingTime();
						platform.enrichMetrics(benchmarkRunResult, getArchiveDirectory(benchmarkRun));
						BenchmarkMetric granulaProcTime = benchmarkRunResult.getMetrics().getProcessingTime();

						// Both standard and granula methods must report processing time.
						if (standardProcTime.isNan() || granulaProcTime.isNan()) {
							LOG.error(String.format("Failed to find metric T_proc for [%s].", benchmarkRun.getId()));
							benchmarkRunResult.getFailures().add(BenchmarkFailure.MET);
						} else {
							// Verify standard and granula methods both report theprocessing time within 1 ms or 1% difference.
							double epilson = Math.max(0.001, standardProcTime.getValue().doubleValue() * 0.01);
							BigDecimal diff = standardProcTime.getValue().subtract(granulaProcTime.getValue());
							if (Math.abs(diff.doubleValue()) > epilson) {
								LOG.error(String.format("Failed to find consistent T_proc [diff(std=%s, granula=%s) = %s] for [%s].",
										standardProcTime, granulaProcTime, diff, benchmarkRun.getId()));
								benchmarkRunResult.getFailures().add(BenchmarkFailure.MET);
							} else {
								LOG.info(String.format("Succeed to find consistent T_proc [diff(std=%s, granula=%s) = %s] for [%s].",
										standardProcTime, granulaProcTime, diff, benchmarkRun.getId()));
							}
						}

					} catch (Exception ex) {
						LOG.error("Failed to generate Granula archives for the benchmark results:", ex);
					}
				} else {
					LOG.error("Skipped generation of Granula archive due to benchmark failure.");
				}

			}
		}
	}


	@Override
	public void postBenchmarkSuite(Benchmark benchmark, BenchmarkResult benchmarkResult) {
	}


	@Override
	public void preReportGeneration(BenchmarkReportGenerator reportGenerator) {
		if (enabled) {
			if (archivingEnabled) {
				if (reportGenerator instanceof HtmlBenchmarkReportGenerator) {
					HtmlBenchmarkReportGenerator htmlReportGenerator = (HtmlBenchmarkReportGenerator)reportGenerator;
					htmlReportGenerator.registerPlugin(new GranulaHtmlGenerator());
				}
			}
		}
	}

	@Override
	public void shutdown() {

	}


	private void loadConfiguration() {
		// Load Granula configuration
		Configuration config;
		try {
			config = ConfigurationUtil.loadConfiguration("granula.properties");
			enabled = config.getBoolean(GRANULA_ENABLED, false);
			platformLogEnabled = config.getBoolean(PLATFORM_LOGGING_ENABLED, false);
			envLogEnabled = config.getBoolean(ENVIRONMENT_LOGGING_ENABLED, false);
			archivingEnabled = config.getBoolean(ARCHIVING_ENABLED, false);

			if(enabled) {
				LOG.info("Granula plugin is found, and is enabled.");
				LOG.info(String.format(" - Logging is %s for Granula.", (platformLogEnabled) ? "enabled" : "disabled"));
				LOG.info(String.format(" - Archiving is %s for Granula.", (archivingEnabled) ? "enabled" : "disabled"));
			} else {
				LOG.info("Granula plugin is found, but is disabled.");
			}

			if (archivingEnabled && !platformLogEnabled) {
				LOG.error(String.format("The archiving feature (%s) is not usable while logging feature (%s) is not enabled. " +
						"Turning off the archiving feature of Granula. ", ARCHIVING_ENABLED, PLATFORM_LOGGING_ENABLED));
				enabled = false;
			}
		} catch (InvalidConfigurationException e) {
			LOG.info("Could not find or load granula.properties.");
		}
	}


	public void preserveExecutionLog(GranulaAwarePlatform platform, BenchmarkRun benchmarkRun, Path benchmarkLogDir) {
		Path backupPath = benchmarkLogDir.resolve("execution");
		backupPath.toFile().mkdirs();

		Path backFile = backupPath.resolve("execution-log.js");

		Execution execution = new Execution();
		execution.setPlatform(platform.getPlatformName());
		execution.setAlgorithm(benchmarkRun.getAlgorithm().getName());
		execution.setDataset(benchmarkRun.getFormattedGraph().getName());
		execution.setJobId(benchmarkRun.getId());
		execution.setLogPath(benchmarkLogDir.toAbsolutePath().toString());
		execution.setStartTime(System.currentTimeMillis());

		FileUtil.writeFile(JsonUtil.toJson(execution), backFile);
	}

	private void readArchive(BenchmarkRunResult benchmarkRunResult) {
		Path arcPath = getArchiveDirectory(benchmarkRunResult.getBenchmarkRun());


	}

	private void createArchive(RunSpecification runSpecification, BenchmarkRunResult benchmarkRunResult) {

		Path logPath = runSpecification.getBenchmarkRunSetup().getLogDir();
		Path arcPath = getArchiveDirectory(benchmarkRunResult.getBenchmarkRun());

		Path driverLogPath = logPath.resolve("execution").resolve("execution-log.js");
		Execution execution = (Execution) JsonUtil.fromJson(FileUtil.readFile(driverLogPath), Execution.class);

		try {
			Files.createDirectories(logPath.resolve("platform"));
			Files.createDirectories(logPath.resolve("environment"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		execution.setStartTime(benchmarkRunResult.getStatus().getStartOfBenchmark().getTime());
		execution.setEndTime(benchmarkRunResult.getStatus().getEndOfBenchmark().getTime());
		execution.setArcPath(arcPath.toAbsolutePath().toString());
		JobModel jobModel = new JobModel(getPlatformModelByMagic(execution.getPlatform()));

		GranulaExecutor granulaExecutor = new GranulaExecutor();
		granulaExecutor.setExecution(execution);
		granulaExecutor.setEnvEnabled(envLogEnabled);
		granulaExecutor.buildJobArchive(jobModel);
	}


	public static PlatformModel getPlatformModelByMagic(String platformName) {

		String modelClassName = String.format("science.atlarge.granula.modeller.platform.%s", StringUtils.capitalize(platformName));

		Class<? extends PlatformModel> modelClass;
		try {
			Class<?> modelClassUncasted = Class.forName(modelClassName);
			modelClass = modelClassUncasted.asSubclass(PlatformModel.class);
		} catch (ClassNotFoundException e) {
			throw new GraphalyticsLoaderException("Could not find class \"" + modelClassName + "\".", e);
		}

		PlatformModel platformModel = null;

		try {
			platformModel = modelClass.newInstance();
		} catch (Exception e) {
			throw new GraphalyticsLoaderException("Could not load class \"" + modelClassName + "\".", e);
		}

		return platformModel;
	}

	public static PlatformModel getPlatformModel(String platformName) {

		InputStream platformFileStream = GranulaPlugin.class.getResourceAsStream("/" + platformName + ".model");
		if (platformFileStream == null) {
			throw new GraphalyticsLoaderException("Missing resource \"" + platformName + ".model\".");
		}

		String modelClassName;
		try (Scanner platformScanner = new Scanner(platformFileStream)) {
			String line = null;
			if (!platformScanner.hasNext()) {
				throw new GraphalyticsLoaderException("Expected a single line with a class name in \"" + platformName +
						".model\", got an empty file.");
			}
			line = platformScanner.next();
			while(line.trim().equals("")) {
				line = platformScanner.next();
			}
			modelClassName = line;
		}

		Class<? extends PlatformModel> modelClass;
		try {
			Class<?> modelClassUncasted = Class.forName(modelClassName);
			modelClass = modelClassUncasted.asSubclass(PlatformModel.class);
		} catch (ClassNotFoundException e) {
			throw new GraphalyticsLoaderException("Could not find class \"" + modelClassName + "\".", e);
		}

		PlatformModel platformModel = null;

		try {
			platformModel = modelClass.newInstance();
		} catch (Exception e) {
			throw new GraphalyticsLoaderException("Could not load class \"" + modelClassName + "\".", e);
		}

		return platformModel;
	}

	private Path getArchiveDirectory(BenchmarkRun benchmarkRun) {
		try {
			return reportWriter.getOrCreateReportPath().resolve(ARC_DIR).resolve(benchmarkRun.getId());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
