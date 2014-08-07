package org.tudelft.graphalytics;

import java.util.Date;

public class BenchmarkRun {

	private final AlgorithmType algorithmType;
	private final Graph graph;
	
	public BenchmarkRun(AlgorithmType algorithmType, Graph graph) {
		this.algorithmType = algorithmType;
		this.graph = graph;
	}
	
	public BenchmarkRunResult executeOnPlatform(Platform platform) {
		BenchmarkRunResult results = new BenchmarkRunResult();
		
		results.setStartOfBenchmarkRun(new Date());
		platform.executeAlgorithmOnGraph(getAlgorithmType(), getGraph().getName());
		results.setEndOfBenchmarkRun(new Date());
		
		return results;
	}
	
	public AlgorithmType getAlgorithmType() {
		return algorithmType;
	}
	
	public Graph getGraph() {
		return graph;
	}
	
}
