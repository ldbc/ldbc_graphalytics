package nl.tudelft.graphalytics.reporting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Graph {

	private String name;
	private Map<String, Result> results;
	
	public Graph(String name) {
		this.name = name;
		this.results = new HashMap<>();
	}
	
	public void addResult(String algorithmName, Result result) {
		this.results.put(algorithmName, result);
	}
	
	public Collection<String> getAlgorithms() {
		return results.keySet();
	}
	
	public Map<String, Result> getResults() {
		return results;
	}
	
	public String getName() {
		return name;
	}
	
}
