package org.tudelft.graphalytics.giraph;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;
import org.tudelft.graphalytics.Graph;
import org.tudelft.graphalytics.Platform;
import org.tudelft.graphalytics.algorithms.AlgorithmType;
import org.tudelft.graphalytics.giraph.bfs.BFSJob;

public class GiraphPlatform implements Platform {

	@Override
	public void uploadGraph(String graphName, String graphFilePath)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean executeAlgorithmOnGraph(AlgorithmType algorithmType,
			Graph graph, Object parameters) {
		try {
			ToolRunner.run(new Configuration(), new BFSJob("/test-giraph-in/sample-graph", "/gout"), new String[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
		return false;
	}

	@Override
	public void deleteGraph(String graphName) {
		// TODO Auto-generated method stub
		
	}

}
