package org.tudelft.graphalytics;

public interface Platform {

	void uploadGraph(String graphName, String graphFilePath);
	void executeAlgorithmOnGraph(AlgorithmType algorithmType, String graphName);
	void deleteGraph(String graphName);
	
}
