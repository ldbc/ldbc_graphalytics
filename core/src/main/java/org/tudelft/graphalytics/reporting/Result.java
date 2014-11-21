package org.tudelft.graphalytics.reporting;

public class Result {

	private Algorithm algorithm;
	private Graph graph;
	private boolean succeeded;
	private long runtimeMs;
	
	public Result(Algorithm algorithm, Graph graph) {
		this.algorithm = algorithm;
		this.graph = graph;
		this.succeeded = false;
		this.runtimeMs = 0;
	}

	public boolean getSucceeded() {
		return succeeded;
	}

	public void setSucceeded(boolean succeeded) {
		this.succeeded = succeeded;
	}

	public long getRuntimeMs() {
		return runtimeMs;
	}

	public void setRuntimeMs(long runtimeMs) {
		this.runtimeMs = runtimeMs;
	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}

	public Graph getGraph() {
		return graph;
	}
	
}
