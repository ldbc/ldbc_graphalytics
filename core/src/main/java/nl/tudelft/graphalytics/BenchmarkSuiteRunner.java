package nl.tudelft.graphalytics;

import nl.tudelft.graphalytics.domain.*;
import nl.tudelft.graphalytics.domain.BenchmarkResult.BenchmarkResultBuilder;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult.BenchmarkSuiteResultBuilder;
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
						new PlatformBenchmarkResult(PlatformConfiguration.empty());
				boolean completedSuccessfully = platform.executeAlgorithmOnGraph(benchmark.getAlgorithm(),
						benchmark.getGraph(), benchmark.getAlgorithmParameters());

				// Stop the timer
				benchmarkResultBuilder.markEndOfBenchmark(completedSuccessfully);
				// Construct the BenchmarkResult and register it
				BenchmarkResult benchmarkResult = benchmarkResultBuilder.buildFromResult(platformBenchmarkResult);
				benchmarkSuiteResultBuilder.withBenchmarkResult(benchmarkResult);
			}

			// Delete the graph
			platform.deleteGraph(graph.getName());
		}

		// Construct the BenchmarkSuiteResult
		return benchmarkSuiteResultBuilder.buildFromConfiguration(SystemConfiguration.empty(),
				PlatformConfiguration.empty());
	}

}
