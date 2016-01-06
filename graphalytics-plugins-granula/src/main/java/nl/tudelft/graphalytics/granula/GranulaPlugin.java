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

import nl.tudelft.graphalytics.domain.Benchmark;
import nl.tudelft.graphalytics.domain.BenchmarkResult;
import nl.tudelft.graphalytics.domain.BenchmarkSuite;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult;
import nl.tudelft.graphalytics.plugin.Plugin;
import nl.tudelft.graphalytics.reporting.BenchmarkReportFile;
import nl.tudelft.graphalytics.reporting.BenchmarkReportGenerator;
import nl.tudelft.graphalytics.reporting.BenchmarkReportWriter;
import nl.tudelft.graphalytics.reporting.html.HtmlBenchmarkReportGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Created by tim on 12/11/15.
 */
public class GranulaPlugin implements Plugin {

	private static final Logger LOG = LogManager.getLogger();

	private static final String LOG_DIR = "log";

	private final GranulaAwarePlatform platform;
	private final BenchmarkReportWriter reportWriter;
	private final GranulaManager granulaManager;

	public GranulaPlugin(GranulaAwarePlatform platform, BenchmarkReportWriter reportWriter) {
		this.platform = platform;
		this.reportWriter = reportWriter;
		this.granulaManager = new GranulaManager(platform);
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
	public void preBenchmarkSuite(BenchmarkSuite benchmarkSuite) {
		// No operation
	}

	@Override
	public void preBenchmark(Benchmark nextBenchmark) {
		if(GranulaManager.isGranulaEnabled) {
			if(GranulaManager.isLoggingEnabled) {
				LOG.info("- Collecting job logs for Granula.");
				platform.setBenchmarkLogDirectory(getLogDirectory(nextBenchmark));
			}
		}

	}

	@Override
	public void postBenchmark(Benchmark completedBenchmark, BenchmarkResult benchmarkResult) {
		if (GranulaManager.isGranulaEnabled) {
			if (GranulaManager.isLoggingEnabled) {
				LOG.info("- Categorizing collected logs for Granula.");
				platform.finalizeBenchmarkLogs(getLogDirectory(completedBenchmark));
			}
		}
	}

	@Override
	public void postBenchmarkSuite(BenchmarkSuite benchmarkSuite, BenchmarkSuiteResult benchmarkSuiteResult) {
		if (GranulaManager.isGranulaEnabled) {
			if (GranulaManager.isArchivingEnabled) {
				LOG.info("- Generating Granula archives from collected job logs.");
				try {
                    granulaManager.setReportDirPath(reportWriter.getOrCreateOutputDataPath());
                    granulaManager.generateArchive(benchmarkSuiteResult);

					if(!GranulaManager.isLogDataPreserved) {
						LOG.info("- Deleting collected job logs.");
						Path logPath = reportWriter.getOrCreateOutputDataPath().resolve("log");
						FileUtils.deleteDirectory(logPath.toFile());
					}
                } catch (IOException ex) {
                    LOG.error("Failed to generate Granula archives for the benchmark results:", ex);
                } catch (Exception ex) {
					LOG.error("Failed to generate Granula archives for the benchmark results:", ex);
				}

			}
		}
	}

	private Path getLogDirectory(Benchmark benchmark) {
		try {
			return reportWriter.getOrCreateOutputDataPath().resolve(LOG_DIR).resolve(benchmark.getBenchmarkIdentificationString());
		} catch (IOException e) {
			// TODO: Add error handling
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void preReportGeneration(BenchmarkReportGenerator reportGenerator) {
		if (GranulaManager.isGranulaEnabled) {
			if (GranulaManager.isArchivingEnabled) {
				LOG.info("- Embedding Granula report into main report.");
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

}
