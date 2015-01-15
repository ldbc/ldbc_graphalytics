package nl.tudelft.graphalytics;

import java.util.Date;

import nl.tudelft.graphalytics.algorithms.AlgorithmType;

public class BenchmarkConfiguration {

	private final AlgorithmType algorithmType;
	private final Graph graph;
	private final Object parameters;
	
	public BenchmarkConfiguration(AlgorithmType algorithmType, Graph graph, Object parameters) {
		this.algorithmType = algorithmType;
		this.graph = graph;
		this.parameters = parameters;
	}
	
	public BenchmarkRunResult executeOnPlatform(Platform platform) {
		BenchmarkRunResult results = new BenchmarkRunResult();
		
		results.setStartOfBenchmarkRun(new Date());
		boolean succes = platform.executeAlgorithmOnGraph(algorithmType, graph, parameters);
		results.setEndOfBenchmarkRun(new Date());
		results.setSucceeded(succes); // TODO: Verify output of job
		
		return results;
	}
	
	public AlgorithmType getAlgorithmType() {
		return algorithmType;
	}
	
	public Graph getGraph() {
		return graph;
	}
	
	public Object getParameters() {
		return parameters;
	}
	
}
