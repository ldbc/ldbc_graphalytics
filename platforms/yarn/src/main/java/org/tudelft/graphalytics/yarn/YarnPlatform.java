package org.tudelft.graphalytics.yarn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ToolRunner;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.tudelft.graphalytics.Graph;
import org.tudelft.graphalytics.Platform;
import org.tudelft.graphalytics.algorithms.AlgorithmType;
import org.tudelft.graphalytics.yarn.bfs.BFSJob;
import org.tudelft.graphalytics.yarn.cd.CDJob;
import org.tudelft.graphalytics.yarn.conn.CONNJob;

public class YarnPlatform implements Platform {
	private static final Logger log = LogManager.getLogger();
	
	private static final Map<AlgorithmType, Class<? extends YarnJob>> jobClassesPerAlgorithm = new HashMap<>();
	
	{
		jobClassesPerAlgorithm.put(AlgorithmType.BFS, BFSJob.class);
		jobClassesPerAlgorithm.put(AlgorithmType.CD, CDJob.class);
		jobClassesPerAlgorithm.put(AlgorithmType.CONN, CONNJob.class);
	}
	
	private Map<String, String> hdfsPathForGraphName = new HashMap<>();
	private int jobCount = 0;

	public void uploadGraph(String graphName, String graphFilePath) throws IOException {
		log.entry(graphName, graphFilePath);
		
		// Establish a connection with HDFS and upload the graph
		Configuration conf = new Configuration();
		conf.set("fs.defaultFS", "hdfs://localhost:9000");
		String hdfsPath = "/graphalytics/input/" + graphName;
		FileSystem dfs = FileSystem.get(conf);
		dfs.copyFromLocalFile(new Path(graphFilePath), new Path(hdfsPath));
		hdfsPathForGraphName.put(graphName, hdfsPath);
		
		log.exit();
	}

	public boolean executeAlgorithmOnGraph(AlgorithmType algorithmType, Graph graph, Object parameters) {
		log.entry(algorithmType, graph);
		jobCount++;
		try {
			YarnJob job = jobClassesPerAlgorithm.get(algorithmType).newInstance();
			job.parseGraphData(graph, parameters);
			job.setInputPath(hdfsPathForGraphName.get(graph.getName()));
			job.setIntermediatePath("/graphalytics/intermediate/job-" + jobCount);
			job.setOutputPath("/graphalytics/output/job-" + jobCount);
			
			String[] args = new String[] {
				"-fs", "hdfs://localhost:9000",
				"-jt", "localhost:8031"
			};
			ToolRunner.run(new Configuration(), job, args);
		} catch (Exception e) {
			log.catching(e);
			return log.exit(false);
		}
		return log.exit(true);
	}

	public void deleteGraph(String graphName) {
		// TODO Auto-generated method stub
		log.entry(graphName);
		
		log.exit();
	}

}
