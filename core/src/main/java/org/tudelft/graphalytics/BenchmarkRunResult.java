package org.tudelft.graphalytics;

import java.util.Date;

public class BenchmarkRunResult {

	private Date startOfBenchmarkRun;
	private Date endOfBenchmarkRun;

	public BenchmarkRunResult() {

	}

	public Date getStartOfBenchmarkRun() {
		return startOfBenchmarkRun;
	}

	public Date getEndOfBenchmarkRun() {
		return endOfBenchmarkRun;
	}

	public long getElapsedTimeInMillis() {
		return endOfBenchmarkRun.getTime() - startOfBenchmarkRun.getTime();
	}

	public void setStartOfBenchmarkRun(Date startOfBenchmarkRun) {
		this.startOfBenchmarkRun = startOfBenchmarkRun;
	}

	public void setEndOfBenchmarkRun(Date endOfBenchmarkRun) {
		this.endOfBenchmarkRun = endOfBenchmarkRun;
	}

}
