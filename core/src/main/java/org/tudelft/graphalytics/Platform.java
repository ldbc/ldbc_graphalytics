package org.tudelft.graphalytics;

import java.io.IOException;

import org.tudelft.graphalytics.algorithms.AlgorithmType;

public interface Platform {

	void uploadGraph(String graphName, String graphFilePath) throws IOException;
	boolean executeAlgorithmOnGraph(AlgorithmType algorithmType, Graph graph, Object parameters);
	void deleteGraph(String graphName);
	
}
