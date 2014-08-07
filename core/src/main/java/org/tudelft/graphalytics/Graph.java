package org.tudelft.graphalytics;

public class Graph {

	private String name;
	private String relativeFilePath;
	
	public Graph(String name, String relativeFilePath) {
		this.name = name;
		this.relativeFilePath = relativeFilePath;
	}
	
	public String getName() {
		return name;
	}
	
	public String getRelativeFilePath() {
		return relativeFilePath;
	}
	
}
