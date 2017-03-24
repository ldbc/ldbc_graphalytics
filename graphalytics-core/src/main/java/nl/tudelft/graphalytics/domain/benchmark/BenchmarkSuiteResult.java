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
package nl.tudelft.graphalytics.domain.benchmark;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	private static final Logger LOG = LogManager.getLogger();
	private final BenchmarkSuite benchmarkSuite;
	private final Collection<BenchmarkResult> benchmarkResults;

	private long totalDuration = 0;

	/**
	 * @param benchmarkSuite         the benchmark suite for which this result was obtained
	 * @param benchmarkResults       the collection of individual benchmark results for each benchmark in the suite
	 */
	private BenchmarkSuiteResult(BenchmarkSuite benchmarkSuite, Collection<BenchmarkResult> benchmarkResults, long totalDuration) {
		this.benchmarkSuite = benchmarkSuite;
		this.benchmarkResults = benchmarkResults;
		this.totalDuration = totalDuration;
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
	 * Factory for creating a new BenchmarkSuiteResult. Guarantees that each benchmark in the suite has
	 * exactly one result associated with it.
	 */
	public static class BenchmarkSuiteResultBuilder {
		private final Map<String, BenchmarkResult> benchmarkResultMap = new HashMap<>();
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
//			if (!benchmarkSuite.getBenchmarks().contains(benchmarkResult.getBenchmarkRun()))
//				throw new IllegalArgumentException("\"benchmarkResult\" must refer to a benchmark that is part of the suite.");

			benchmarkResultMap.put(benchmarkResult.getBenchmarkRun().getId(), benchmarkResult);
			return this;
		}

		public BenchmarkSuiteResultBuilder withoutBenchmarkResult(BenchmarkRun benchmarkRun) {
			benchmarkResultMap.put(benchmarkRun.getId(), BenchmarkResult.forBenchmarkNotRun(benchmarkRun));
			return this;
		}

		/**
		 * Builds the BenchmarkSuiteResult object with the given configuration details.
		 *
		 * @param systemDetails          the configuration of the system used to run the benchmark suite
		 * @return a new BenchmarkSuiteResult
		 * @throws IllegalArgumentException iff systemConfiguration is null or platformConfiguration is null
		 */
		public BenchmarkSuiteResult buildFromConfiguration(long totalDuration) {

			// Add benchmark results ("not run") for any benchmark that does not have a corresponding result
			for (BenchmarkRun benchmarkRun : benchmarkSuite.getBenchmarkRuns()) {
				if (!benchmarkResultMap.containsKey(benchmarkRun.getId())) {
					LOG.warn(String.format("Benchmark %s has no results!", benchmarkRun.getId()));
					benchmarkResultMap.put(benchmarkRun.getId(), BenchmarkResult.forBenchmarkNotRun(benchmarkRun));
				}
			}

			return new BenchmarkSuiteResult(benchmarkSuite, benchmarkResultMap.values(), totalDuration);
		}

	}

	public long getTotalDuration() {
		return totalDuration;
	}

}
