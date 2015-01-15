package nl.tudelft.graphalytics;

import nl.tudelft.graphalytics.algorithms.AlgorithmType;

public interface Platform {

	void uploadGraph(Graph graph, String graphFilePath) throws Exception;
	boolean executeAlgorithmOnGraph(AlgorithmType algorithmType, Graph graph, Object parameters);
	void deleteGraph(String graphName);
	
	public String getName();
	
}
