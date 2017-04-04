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
package science.atlarge.graphalytics.plugin;

import science.atlarge.graphalytics.domain.benchmark.Benchmark;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.report.result.BenchmarkResult;
import science.atlarge.graphalytics.report.result.BenchmarkSuiteResult;
import science.atlarge.graphalytics.report.BenchmarkReportGenerator;

/**
 * @author Tim Hegeman
 */
public interface Plugin {

	void preBenchmarkSuite(Benchmark benchmark);

	void preBenchmark(BenchmarkRun nextBenchmarkRun);


	void prepare(BenchmarkRun benchmarkRun);

	void cleanup(BenchmarkRun benchmarkRun, BenchmarkResult benchmarkResult);

	void postBenchmark(BenchmarkRun benchmarkRun, BenchmarkResult benchmarkResult);

	void postBenchmarkSuite(Benchmark benchmark, BenchmarkSuiteResult benchmarkSuiteResult);

	void preReportGeneration(BenchmarkReportGenerator reportGenerator);

	void shutdown();

	String getPluginName();

	String getPluginDescription();

}
