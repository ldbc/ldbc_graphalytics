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
import nl.tudelft.graphalytics.plugin.Plugin;
import nl.tudelft.graphalytics.reporting.BenchmarkReportGenerator;
import nl.tudelft.graphalytics.reporting.BenchmarkReportWriter;

import java.io.IOException;

/**
 * Created by tim on 12/11/15.
 */
public class GranulaPlugin implements Plugin {

	private static final String LOG_DIR = "logs";

	private final GranulaAwarePlatform platform;
	private final BenchmarkReportWriter reportWriter;

	public GranulaPlugin(GranulaAwarePlatform platform, BenchmarkReportWriter reportWriter) {
		this.platform = platform;
		this.reportWriter = reportWriter;
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
	public void preBenchmark(Benchmark nextBenchmark) {
		try {
			platform.setBenchmarkLogDirectory(reportWriter.getOrCreateOutputDataPath().resolve(LOG_DIR));
		} catch (IOException e) {
			// TODO: Add error handling
			e.printStackTrace();
		}
	}

	@Override
	public void postBenchmark(Benchmark completedBenchmark) {
		platform.finalizeBenchmarkLogs();
	}

	@Override
	public void preReportGeneration(BenchmarkReportGenerator reportGenerator) {

	}

	@Override
	public void shutdown() {

	}

}
