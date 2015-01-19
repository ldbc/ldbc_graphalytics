package nl.tudelft.graphalytics;

import java.util.Date;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.Graph;

public class BenchmarkConfiguration {

	private final Algorithm algorithm;
	private final Graph graph;
	private final Object parameters;
	
	public BenchmarkConfiguration(Algorithm algorithm, Graph graph, Object parameters) {
		this.algorithm = algorithm;
		this.graph = graph;
		this.parameters = parameters;
	}
	
	public BenchmarkRunResult executeOnPlatform(Platform platform) {
		BenchmarkRunResult results = new BenchmarkRunResult();
		
		results.setStartOfBenchmarkRun(new Date());
		boolean succes = platform.executeAlgorithmOnGraph(algorithm, graph, parameters);
		results.setEndOfBenchmarkRun(new Date());
		results.setSucceeded(succes); // TODO: Verify output of job
		
		return results;
	}
	
	public Algorithm getAlgorithm() {
		return algorithm;
	}
	
	public Graph getGraph() {
		return graph;
	}
	
	public Object getParameters() {
		return parameters;
	}
	
}
