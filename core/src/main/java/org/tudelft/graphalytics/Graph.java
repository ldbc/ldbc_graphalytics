package org.tudelft.graphalytics;

public class Graph {

	private String name;
	private String relativeFilePath;
	private boolean directed;
	private boolean edgeBased;
	
	public Graph(String name, String relativeFilePath, boolean directed, boolean edgeBased) {
		this.name = name;
		this.relativeFilePath = relativeFilePath;
		this.directed = directed;
		this.edgeBased = edgeBased;
	}
	
	public String getName() {
		return name;
	}
	
	public String getRelativeFilePath() {
		return relativeFilePath;
	}
	
	public boolean isDirected() {
		return directed;
	}
	
	public boolean isEdgeBased() {
		return edgeBased;
	}
	
	@Override
	public String toString() {
		return "Graph(" + name + ")";
	}
	
}
