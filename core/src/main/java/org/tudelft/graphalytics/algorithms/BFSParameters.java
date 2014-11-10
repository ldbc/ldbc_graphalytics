package org.tudelft.graphalytics.algorithms;

public class BFSParameters {
	private final String sourceVertex;
	
	public BFSParameters(String sourceVertex) {
		this.sourceVertex = sourceVertex;
	}
	
	public String getSourceVertex() {
		return sourceVertex;
	}
}
