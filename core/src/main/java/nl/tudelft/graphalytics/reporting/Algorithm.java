package nl.tudelft.graphalytics.reporting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Algorithm {

	private String name;
	private Map<String, Result> results;
	
	public Algorithm(String name) {
		this.name = name;
		this.results = new HashMap<>();
	}
	
	public void addResult(String graphName, Result result) {
		this.results.put(graphName, result);
	}
	
	public Collection<String> getGraphs() {
		return results.keySet();
	}
	
	public Map<String, Result> getResults() {
		return results;
	}
	
	public String getName() {
		return name;
	}
	
}
