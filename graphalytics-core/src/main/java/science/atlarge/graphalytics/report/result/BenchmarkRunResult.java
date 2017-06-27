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

import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.execution.BenchmarkFailures;
import science.atlarge.graphalytics.execution.BenchmarkStatus;

import java.io.Serializable;

/**
 * Results of the execution of a single benchmark. Includes timestamps to compute the makespan, a flag for successful
 * completion, and platform-specific information about the benchmark execution.
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public final class BenchmarkRunResult implements Serializable {

	private BenchmarkRun benchmarkRun;
	private BenchmarkStatus status;
	private BenchmarkFailures failures;
	private BenchmarkMetrics metrics;

	/**
	 * @param benchmarkRun the benchmark executed to obtain this result
	 */
	public BenchmarkRunResult(BenchmarkRun benchmarkRun, BenchmarkStatus status,
							  BenchmarkFailures failures, BenchmarkMetrics metrics) {
		this.benchmarkRun = benchmarkRun;
		this.failures = failures;
		this.metrics = metrics;
		this.status = status;
	}

	/**
	 * Creates an empty BenchmarkRunResult for a benchmark that has not been run.
	 *
	 * @param benchmarkRun the benchmark that has not been run
	 * @return a new empty BenchmarkRunResult
	 */
	public static BenchmarkRunResult emptyBenchmarkRun(BenchmarkRun benchmarkRun) {
		return new BenchmarkRunResult(benchmarkRun, new BenchmarkStatus(), new BenchmarkFailures(), new BenchmarkMetrics());
	}

	public BenchmarkMetrics getMetrics() {
		return metrics;
	}

	/**
	 * @return the benchmark executed to obtain this result
	 */
	public BenchmarkRun getBenchmarkRun() {
		return benchmarkRun;
	}

	public BenchmarkFailures getFailures() {
		return failures;
	}

	public BenchmarkStatus getStatus() {
		return status;
	}

	public boolean isSuccessful() {
		return status.isValidated() && failures.hasNone();
	}
}
