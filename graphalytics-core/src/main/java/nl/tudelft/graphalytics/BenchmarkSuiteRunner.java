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

import nl.tudelft.graphalytics.domain.*;
import nl.tudelft.graphalytics.domain.BenchmarkResult.BenchmarkResultBuilder;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult.BenchmarkSuiteResultBuilder;
import nl.tudelft.graphalytics.plugin.Plugins;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Helper class for executing all benchmarks in a BenchmarkSuite on a specific Platform.
 *
 * @author Tim Hegeman
 */
public class BenchmarkSuiteRunner {
	private static final Logger LOG = LogManager.getLogger();

	private final BenchmarkSuite benchmarkSuite;
	private final Platform platform;
	private final Plugins plugins;

	/**
	 * @param benchmarkSuite the suite of benchmarks to run
	 * @param platform       the platform instance to run the benchmarks on
	 * @param plugins        collection of loaded plugins
	 */
	public BenchmarkSuiteRunner(BenchmarkSuite benchmarkSuite, Platform platform, Plugins plugins) {
		this.benchmarkSuite = benchmarkSuite;
		this.platform = platform;
		this.plugins = plugins;
	}

	/**
	 * Executes the Graphalytics benchmark suite on the given platform. The benchmarks are grouped by graph so that each
	 * graph is uploaded to the platform exactly once. After executing all benchmarks for a specific graph, the graph
	 * is deleted from the platform.
	 *
	 * @return a BenchmarkSuiteResult object containing the gathered benchmark results and details
	 */
	public BenchmarkSuiteResult execute() {
		// TODO: Retrieve configuration for system, platform, and platform per benchmark

		// Use a BenchmarkSuiteResultBuilder to track the benchmark results gathered throughout execution
		BenchmarkSuiteResultBuilder benchmarkSuiteResultBuilder = new BenchmarkSuiteResultBuilder(benchmarkSuite);

		for (Graph graph : benchmarkSuite.getGraphs()) {
			// Upload the graph
			try {
				platform.uploadGraph(graph);
			} catch (Exception ex) {
				LOG.error("Failed to upload graph \"" + graph.getName() + "\", skipping.", ex);
				continue;
			}

			// Execute all benchmarks for this graph
			for (Benchmark benchmark : benchmarkSuite.getBenchmarksForGraph(graph)) {
				// Use a BenchmarkResultBuilder to create the BenchmarkResult for this Benchmark
				BenchmarkResultBuilder benchmarkResultBuilder = new BenchmarkResultBuilder(benchmark);

				LOG.info("Benchmarking algorithm \"" + benchmark.getAlgorithm().getName() + "\" on graph \"" +
						graph.getName() + ".");

				// Execute the pre-benchmark steps of all plugins
				plugins.preBenchmark(benchmark);

				// Start the timer
				benchmarkResultBuilder.markStartOfBenchmark();

				// Execute the benchmark and collect the result
				PlatformBenchmarkResult platformBenchmarkResult =
						new PlatformBenchmarkResult(NestedConfiguration.empty());
				boolean completedSuccessfully = false;
				try {
					platformBenchmarkResult = platform.executeAlgorithmOnGraph(benchmark);
					completedSuccessfully = true;
				} catch (PlatformExecutionException ex) {
					LOG.error("Algorithm \"" + benchmark.getAlgorithm().getName() + "\" on graph \"" +
							graph.getName() + " failed to complete:", ex);
				}

				// Stop the timer
				benchmarkResultBuilder.markEndOfBenchmark(completedSuccessfully);

				LOG.info("Benchmarked algorithm \"" + benchmark.getAlgorithm().getName() + "\" on graph \"" +
						graph.getName() + ".");

				// Execute the post-benchmark steps of all plugins
				plugins.postBenchmark(benchmark);

				// Construct the BenchmarkResult and register it
				BenchmarkResult benchmarkResult = benchmarkResultBuilder.buildFromResult(platformBenchmarkResult);
				benchmarkSuiteResultBuilder.withBenchmarkResult(benchmarkResult);
			}

			// Delete the graph
			platform.deleteGraph(graph.getName());
		}

		// Dump the used configuration
		NestedConfiguration benchmarkConfiguration = NestedConfiguration.empty();
		try {
			Configuration configuration = new PropertiesConfiguration("benchmark.properties");
			benchmarkConfiguration = NestedConfiguration.fromExternalConfiguration(configuration,
					"benchmark.properties");
		} catch (ConfigurationException e) {
			// Already reported during loading of benchmark
		}

		// Construct the BenchmarkSuiteResult
		return benchmarkSuiteResultBuilder.buildFromConfiguration(SystemDetails.empty(),
				benchmarkConfiguration,
				platform.getPlatformConfiguration());
	}

}
