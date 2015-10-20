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
package nl.tudelft.graphalytics.domain;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Results of the execution of the Graphalytics benchmark suite on a single platform. Includes configuration details
 * of both the system and platform, in addition to the individual benchmark results.
 *
 * @author Tim Hegeman
 */
public final class BenchmarkSuiteResult implements Serializable {

	private final BenchmarkSuite benchmarkSuite;
	private final Collection<BenchmarkResult> benchmarkResults;

	private final NestedConfiguration benchmarkConfiguration;
	private final NestedConfiguration platformConfiguration;
	private final SystemDetails systemDetails;

	/**
	 * @param benchmarkSuite         the benchmark suite for which this result was obtained
	 * @param benchmarkResults       the collection of individual benchmark results for each benchmark in the suite
	 * @param benchmarkConfiguration the benchmark configuration used to load graphs, decide which algorithms to run,
	 *                               etc.
	 * @param platformConfiguration  the platform-specific configuration options used during execution of the benchmark
	 *                               suite
	 * @param systemDetails          the configuration of the system used to run the benchmark suite
	 */
	private BenchmarkSuiteResult(BenchmarkSuite benchmarkSuite, Collection<BenchmarkResult> benchmarkResults,
	                             NestedConfiguration benchmarkConfiguration, NestedConfiguration platformConfiguration,
	                             SystemDetails systemDetails) {
		this.benchmarkSuite = benchmarkSuite;
		this.benchmarkResults = benchmarkResults;
		this.benchmarkConfiguration = benchmarkConfiguration;
		this.platformConfiguration = platformConfiguration;
		this.systemDetails = systemDetails;
	}

	/**
	 * @return the benchmark suite for which this result was obtained
	 */
	public BenchmarkSuite getBenchmarkSuite() {
		return benchmarkSuite;
	}

	/**
	 * @return the collection of individual benchmark results for each benchmark in the suite
	 */
	public Collection<BenchmarkResult> getBenchmarkResults() {
		return benchmarkResults;
	}

	/**
	 * @return the benchmark configuration used to load graphs, decide which algorithms to run, etc.
	 */
	public NestedConfiguration getBenchmarkConfiguration() {
		return benchmarkConfiguration;
	}

	/**
	 * @return the platform-specific configuration options used during execution of the benchmark suite
	 */
	public NestedConfiguration getPlatformConfiguration() {
		return platformConfiguration;
	}

	/**
	 * @return the configuration of the system used to run the benchmark suite
	 */
	public SystemDetails getSystemDetails() {
		return systemDetails;
	}

	/**
	 * Factory for creating a new BenchmarkSuiteResult. Guarantees that each benchmark in the suite has
	 * exactly one result associated with it.
	 */
	public static class BenchmarkSuiteResultBuilder {
		private final Map<Benchmark, BenchmarkResult> benchmarkResultMap = new HashMap<>();
		private BenchmarkSuite benchmarkSuite;

		/**
		 * Constructs a new BenchmarkSuiteResultBuilder that can be used to create a new BenchmarkSuiteResult.
		 *
		 * @param benchmarkSuite the benchmark suite for which to collect results
		 * @throws IllegalArgumentException iff benchmarkSuite is null
		 */
		public BenchmarkSuiteResultBuilder(BenchmarkSuite benchmarkSuite) {
			if (benchmarkSuite == null)
				throw new IllegalArgumentException("Parameter \"benchmarkSuite\" must not be null.");

			this.benchmarkSuite = benchmarkSuite;
		}

		/**
		 * Adds a BenchmarkResult to the list of results for this BenchmarkSuite. Overrides any previous result
		 * for the same benchmark.
		 *
		 * @param benchmarkResult a benchmark result to add to the results of the benchmark suite
		 * @return a reference to this
		 * @throws IllegalArgumentException if benchmarkResult is null or if benchmarkResult corresponds to a benchmark
		 *                                  that is not part of the suite
		 */
		public BenchmarkSuiteResultBuilder withBenchmarkResult(BenchmarkResult benchmarkResult) {
			if (benchmarkResult == null)
				throw new IllegalArgumentException("Parameter \"benchmarkResult\" must not be null.");
			if (!benchmarkSuite.getBenchmarks().contains(benchmarkResult.getBenchmark()))
				throw new IllegalArgumentException("\"benchmarkResult\" must refer to a benchmark that is part of the suite.");

			benchmarkResultMap.put(benchmarkResult.getBenchmark(), benchmarkResult);
			return this;
		}

		/**
		 * Builds the BenchmarkSuiteResult object with the given configuration details.
		 *
		 * @param systemDetails          the configuration of the system used to run the benchmark suite
		 * @param benchmarkConfiguration the benchmark configuration used to load graphs, decide which algorithms to
		 *                               run, etc.
		 * @param platformConfiguration  the platform-specific configuration options used during execution of the
		 *                               benchmark suite
		 * @return a new BenchmarkSuiteResult
		 * @throws IllegalArgumentException iff systemConfiguration is null or platformConfiguration is null
		 */
		public BenchmarkSuiteResult buildFromConfiguration(SystemDetails systemDetails,
		                                                   NestedConfiguration benchmarkConfiguration,
		                                                   NestedConfiguration platformConfiguration) {
			if (systemDetails == null)
				throw new IllegalArgumentException("Parameter \"systemConfiguration\" must not be null.");
			if (benchmarkConfiguration == null)
				throw new IllegalArgumentException("Parameter \"benchmarkConfiguration\" must not be null.");
			if (platformConfiguration == null)
				throw new IllegalArgumentException("Parameter \"platformConfiguration\" must not be null.");

			// Add benchmark results ("not run") for any benchmark that does not have a corresponding result
			for (Benchmark benchmark : benchmarkSuite.getBenchmarks()) {
				if (!benchmarkResultMap.containsKey(benchmark))
					benchmarkResultMap.put(benchmark, BenchmarkResult.forBenchmarkNotRun(benchmark));
			}

			return new BenchmarkSuiteResult(benchmarkSuite, benchmarkResultMap.values(), benchmarkConfiguration,
					platformConfiguration, systemDetails);
		}

	}

}
