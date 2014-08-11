package org.tudelft.graphalytics.yarn;

import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.tudelft.graphalytics.AlgorithmType;
import org.tudelft.graphalytics.BenchmarkSuite;
import org.tudelft.graphalytics.Platform;
import org.tudelft.graphalytics.yarn.bfs.BFSJob;

public class YarnPlatform implements Platform {
	private static final Logger log = LogManager.getLogger();
	
	private static final Map<AlgorithmType, Class<? extends YarnJob>> jobClassesPerAlgorithm = new HashMap<>();
	
	{
		jobClassesPerAlgorithm.put(AlgorithmType.BFS, BFSJob.class);
	}

	public void uploadGraph(String graphName, String graphFilePath) {
		// TODO Auto-generated method stub
		log.entry(graphName, graphFilePath);
		
		log.exit();
	}

	public void executeAlgorithmOnGraph(AlgorithmType algorithmType, String graphName) {
		log.entry(algorithmType, graphName);
		try {
			YarnJob job = jobClassesPerAlgorithm.get(algorithmType).newInstance();
			String[] bfsOptions = new String[] {
				"directed",
				"snap",
				"false",
				"false",
				"2",
				"2",
				"/wiki-Vote.txt",
				"/graphalytics/wiki-Vote/output/",
				"11"
			};
			ToolRunner.run(new Configuration(), job, bfsOptions);
		} catch (Exception e) {
			log.catching(e);
			// TODO: Do something with this exception
		}
		log.exit();
	}

	public void deleteGraph(String graphName) {
		// TODO Auto-generated method stub
		log.entry(graphName);
		
		log.exit();
	}
	
	public static void main(String[] args) {
		new BenchmarkSuite("/data/").runOnPlatform(new YarnPlatform());
	}

}
