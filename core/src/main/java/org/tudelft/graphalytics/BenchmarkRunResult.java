package org.tudelft.graphalytics;

import java.util.Date;

public class BenchmarkRunResult {

	private Date startOfBenchmarkRun;
	private Date endOfBenchmarkRun;
	private boolean succeeded = false;

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
	
	public boolean hasSucceeded() {
		return succeeded;
	}

	public void setStartOfBenchmarkRun(Date startOfBenchmarkRun) {
		this.startOfBenchmarkRun = startOfBenchmarkRun;
	}

	public void setEndOfBenchmarkRun(Date endOfBenchmarkRun) {
		this.endOfBenchmarkRun = endOfBenchmarkRun;
	}
	
	public void setSucceeded(boolean succeeded) {
		this.succeeded = succeeded;
	}

}
