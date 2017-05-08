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
package science.atlarge.graphalytics.report.result;

import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;

import java.io.Serializable;
import java.util.Date;

/**
 * Results of the execution of a single benchmark. Includes timestamps to compute the makespan, a flag for successful
 * completion, and platform-specific information about the benchmark execution.
 *
 * @author Tim Hegeman
 */
public final class BenchmarkRunResult implements Serializable {

	private final BenchmarkRun benchmarkRun;
	private final BenchmarkMetrics metrics;

	private final Date startOfBenchmark;
	private final Date endOfBenchmark;
	private final boolean successful;
	private final boolean completed;
	private final boolean validated;

	/**
	 * @param benchmarkRun               the benchmark executed to obtain this result
	 * @param startOfBenchmark        the start time of the benchmark execution
	 * @param endOfBenchmark          the completion time of the benchmark execution
	 * @param successful   true iff the benchmark completed successfully
	 */
	private BenchmarkRunResult(BenchmarkRun benchmarkRun, BenchmarkMetrics metrics,
							   Date startOfBenchmark, Date endOfBenchmark,
							   boolean completed, boolean validated, boolean successful) {
		this.benchmarkRun = benchmarkRun;
		this.metrics = metrics;
		this.startOfBenchmark = startOfBenchmark;
		this.endOfBenchmark = endOfBenchmark;
		this.completed = completed;
		this.validated = validated;
		this.successful = successful;
	}

	/**
	 * Creates an empty BenchmarkRunResult for a benchmark that has not been run.
	 *
	 * @param benchmarkRun the benchmark that has not been run
	 * @return a new empty BenchmarkRunResult
	 */
	public static BenchmarkRunResult forBenchmarkNotRun(BenchmarkRun benchmarkRun) {
		return new BenchmarkRunResult(benchmarkRun, new BenchmarkMetrics(),
				new Date(0), new Date(0), false, false, false);
	}

	public BenchmarkRunResult withUpdatedBenchmarkMetrics(BenchmarkMetrics updatedMetrics) {
		return new BenchmarkRunResult(benchmarkRun, updatedMetrics, startOfBenchmark, endOfBenchmark,
				completed, validated, successful);
	}

	/**
	 * @return the benchmark executed to obtain this result
	 */
	public BenchmarkRun getBenchmarkRun() {
		return benchmarkRun;
	}

	/**
	 * @return the start time of the benchmark execution
	 */
	public Date getStartOfBenchmark() {
		return new Date(startOfBenchmark.getTime());
	}

	/**
	 * @return the completion time of the benchmark execution
	 */
	public Date getEndOfBenchmark() {
		return new Date(endOfBenchmark.getTime());
	}


	public BenchmarkMetrics getMetrics() {
		return metrics;
	}

	/**
	 * @return true iff the benchmark completed successfully
	 */
	public boolean isSuccessful() {
		return successful;
	}

	public boolean isCompleted() {
		return completed;
	}

	public boolean isValidated() {
		return validated;
	}

	/**
	 * @return the elapsed time from start to end in milliseconds
	 */
	public long getElapsedTimeInMillis() {
		return endOfBenchmark.getTime() - startOfBenchmark.getTime();
	}

	/**
	 * Factory class for the BenchmarkRunResult class.
	 */
	public static class BenchmarkResultBuilder {
		private BenchmarkRun benchmarkRun;
		private BenchmarkMetrics metrics;
		private Date startOfBenchmark;
		private Date endOfBenchmark;

		private boolean completed = false;
		private boolean validated = false;
		private boolean successful = false;

		/**
		 * Constructs a new BenchmarkResultBuilder that can be used to create a new BenchmarkRunResult.
		 *
		 * @param benchmarkRun the benchmark to be executed to obtain a new result
		 * @throws IllegalArgumentException iff benchmark is null
		 */
		public BenchmarkResultBuilder(BenchmarkRun benchmarkRun) {
			if (benchmarkRun == null)
				throw new IllegalArgumentException("Parameter \"benchmark\" must not be null.");

			this.benchmarkRun = benchmarkRun;
			startOfBenchmark = endOfBenchmark = new Date();
		}

		/**
		 * Sets the start of the benchmark execution to be the current time.
		 */
		public void markStartOfBenchmark() {
			startOfBenchmark = new Date();
		}

		/**
		 * Sets the end of the benchmark execution to be the current time. Also records the completion
		 * status of the benchmark.
		 *
		 */
		public void markEndOfBenchmark() {
			endOfBenchmark = new Date();
		}

		public void setBenchmarkMetrics(BenchmarkMetrics metrics) {
			this.metrics = metrics;
		}

		public void setCompleted(boolean completed) {
			this.completed = completed;
		}

		public void setValidated(boolean validated) {
			this.validated = validated;
		}

		public void setSuccessful(boolean successful) {
			this.successful = successful;
		}

		public void setMetrics(BenchmarkMetrics metrics) {
			this.metrics = metrics;
		}

		/**
		 * @return a new BenchmarkRunResult
		 * @throws IllegalArgumentException iff benchmark is null
		 */
		public BenchmarkRunResult buildFromResult() {
			return new BenchmarkRunResult(benchmarkRun, metrics,
					startOfBenchmark, endOfBenchmark, completed, validated, successful);
		}
	}

}
