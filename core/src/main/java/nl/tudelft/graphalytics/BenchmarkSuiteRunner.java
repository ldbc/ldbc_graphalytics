package nl.tudelft.graphalytics;

import nl.tudelft.graphalytics.domain.*;
import nl.tudelft.graphalytics.domain.BenchmarkResult.BenchmarkResultBuilder;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult.BenchmarkSuiteResultBuilder;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Helper class for executing all benchmarks in a BenchmarkSuite on a specific Platform.
 *
 * @author Tim Hegeman
 */
public class BenchmarkSuiteRunner {
	private static final Logger LOG = LogManager.getLogger();

	private final BenchmarkSuite benchmarkSuite;
	private final Platform platform;

	/**
	 * @param benchmarkSuite the suite of benchmarks to run
	 * @param platform the platform instance to run the benchmarks on
	 */
	public BenchmarkSuiteRunner(BenchmarkSuite benchmarkSuite, Platform platform) {
		this.benchmarkSuite = benchmarkSuite;
		this.platform = platform;
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
				platform.uploadGraph(graph, graph.getFilePath());
			} catch (Exception ex) {
				LOG.error("Failed to upload graph \"" + graph.getName() + "\", skipping.", ex);
				continue;
			}

			// Execute all benchmarks for this graph
			for (Benchmark benchmark : benchmarkSuite.getBenchmarksForGraph(graph)) {
				// Use a BenchmarkResultBuilder to create the BenchmarkResult for this Benchmark
				BenchmarkResultBuilder benchmarkResultBuilder = new BenchmarkResultBuilder(benchmark);
				// Start the timer
				benchmarkResultBuilder.markStartOfBenchmark();

				// Execute the benchmark and collect the result
				PlatformBenchmarkResult platformBenchmarkResult =
						new PlatformBenchmarkResult(NestedConfiguration.empty());
				boolean completedSuccessfully = false;
				try {
					platformBenchmarkResult = platform.executeAlgorithmOnGraph(benchmark.getAlgorithm(),
							benchmark.getGraph(), benchmark.getAlgorithmParameters());
					completedSuccessfully = true;
				} catch (PlatformExecutionException ex) {
					LOG.error("Algorithm \"" + benchmark.getAlgorithm().getName() + "\" on graph \"" +
							graph.getName() + " failed to complete:", ex);
				}

				// Stop the timer
				benchmarkResultBuilder.markEndOfBenchmark(completedSuccessfully);
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
