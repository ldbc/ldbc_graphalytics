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
package science.atlarge.graphalytics.report.result;

import science.atlarge.graphalytics.domain.benchmark.Benchmark;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
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
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public final class BenchmarkResult implements Serializable {

	private static final Logger LOG = LogManager.getLogger();
	private final Benchmark benchmark;
	private final Collection<BenchmarkRunResult> benchmarkRunResults;

	private long totalDuration = 0;

	/**
	 * @param benchmark         the benchmark suite for which this result was obtained
	 * @param benchmarkRunResults       the collection of individual benchmark results for each benchmark in the suite
	 */
	private BenchmarkResult(Benchmark benchmark, Collection<BenchmarkRunResult> benchmarkRunResults, long totalDuration) {
		this.benchmark = benchmark;
		this.benchmarkRunResults = benchmarkRunResults;
		this.totalDuration = totalDuration;
	}

	/**
	 * @return the benchmark suite for which this result was obtained
	 */
	public Benchmark getBenchmark() {
		return benchmark;
	}

	/**
	 * @return the collection of individual benchmark results for each benchmark in the suite
	 */
	public Collection<BenchmarkRunResult> getBenchmarkRunResults() {
		return benchmarkRunResults;
	}

	/**
	 * Factory for creating a new BenchmarkResult. Guarantees that each benchmark in the suite has
	 * exactly one result associated with it.
	 */
	public static class BenchmarkSuiteResultBuilder {
		private final Map<String, BenchmarkRunResult> benchmarkResultMap = new HashMap<>();
		private Benchmark benchmark;

		/**
		 * Constructs a new BenchmarkSuiteResultBuilder that can be used to create a new BenchmarkResult.
		 *
		 * @param benchmark the benchmark suite for which to collect results
		 * @throws IllegalArgumentException iff benchmark is null
		 */
		public BenchmarkSuiteResultBuilder(Benchmark benchmark) {
			if (benchmark == null)
				throw new IllegalArgumentException("Parameter \"benchmark\" must not be null.");

			this.benchmark = benchmark;
		}

		/**
		 * Adds a BenchmarkRunResult to the list of results for this Benchmark. Overrides any previous result
		 * for the same benchmark.
		 *
		 * @param benchmarkRunResult a benchmark result to add to the results of the benchmark suite
		 * @return a reference to this
		 * @throws IllegalArgumentException if benchmarkRunResult is null or if benchmarkRunResult corresponds to a benchmark
		 *                                  that is not part of the suite
		 */
		public BenchmarkSuiteResultBuilder withBenchmarkResult(BenchmarkRunResult benchmarkRunResult) {
			if (benchmarkRunResult == null)
				throw new IllegalArgumentException("Parameter \"benchmarkRunResult\" must not be null.");
//			if (!benchmark.getBenchmarks().contains(benchmarkRunResult.getBenchmarkRun()))
//				throw new IllegalArgumentException("\"benchmarkRunResult\" must refer to a benchmark that is part of the suite.");

			benchmarkResultMap.put(benchmarkRunResult.getBenchmarkRun().getId(), benchmarkRunResult);
			return this;
		}

		public BenchmarkSuiteResultBuilder withoutBenchmarkResult(BenchmarkRun benchmarkRun) {
			benchmarkResultMap.put(benchmarkRun.getId(), BenchmarkRunResult.emptyBenchmarkRun(benchmarkRun));
			return this;
		}

		public BenchmarkSuiteResultBuilder withFailedBenchmarkResult(BenchmarkRunResult benchmarkRunResult) {
			if (benchmarkRunResult == null)
				throw new IllegalArgumentException("Parameter \"benchmarkRunResult\" must not be null.");

			benchmarkResultMap.put(benchmarkRunResult.getBenchmarkRun().getId(), benchmarkRunResult);
			return this;
		}

		/**
		 * Builds the BenchmarkResult object with the given configuration details.
		 *
		 * @return a new BenchmarkResult
		 * @throws IllegalArgumentException iff systemConfiguration is null or platformConfiguration is null
		 */
		public BenchmarkResult buildFromConfiguration(long totalDuration) {

			// Add benchmark results ("not run") for any benchmark that does not have a corresponding result
			for (BenchmarkRun benchmarkRun : benchmark.getBenchmarkRuns()) {
				if (!benchmarkResultMap.containsKey(benchmarkRun.getId())) {
					LOG.warn(String.format("Benchmark %s has no results!", benchmarkRun.getId()));
					benchmarkResultMap.put(benchmarkRun.getId(), BenchmarkRunResult.emptyBenchmarkRun(benchmarkRun));
				}
			}

			return new BenchmarkResult(benchmark, benchmarkResultMap.values(), totalDuration);
		}

	}

	public long getTotalDuration() {
		return totalDuration;
	}

}
