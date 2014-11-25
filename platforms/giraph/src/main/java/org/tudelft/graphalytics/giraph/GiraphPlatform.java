package org.tudelft.graphalytics.giraph;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ToolRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tudelft.graphalytics.Graph;
import org.tudelft.graphalytics.Platform;
import org.tudelft.graphalytics.algorithms.AlgorithmType;
import org.tudelft.graphalytics.giraph.bfs.BFSJob;
import org.tudelft.graphalytics.mapreduceutils.io.DirectedEdgeToVertexOutConversion;

public class GiraphPlatform implements Platform {
	private static final Logger LOG = LogManager.getLogger();

	private Map<String, String> pathsOfGraphs = new HashMap<>();
	private int jobCounter = 0;
	
	@Override
	public void uploadGraph(Graph graph, String graphFilePath) throws Exception {
		LOG.entry(graph, graphFilePath);
		
		String tempPath = "/graphalytics-giraph/raw-input/" + graph.getName();
		String processedPath = "/graphalytics-giraph/input/" + graph.getName();
		
		// Upload the raw data to HDFS
		FileSystem fs = FileSystem.get(new Configuration());
		fs.copyFromLocalFile(new Path(graphFilePath), new Path(tempPath));
		
		// Preprocess the graph to an adjacency list format
		if (graph.isDirected() && graph.isEdgeBased()) {
			DirectedEdgeToVertexOutConversion conversion = new DirectedEdgeToVertexOutConversion(tempPath, processedPath);
			conversion.run();
		} else {
			LOG.throwing(new NotImplementedException(
					"Graphalytics Giraph currently only supports directed, edge-based graphs."));
		}
		
		// Remove the raw data
		fs.delete(new Path(graphFilePath), true);
		fs.close();
		
		// Track available datasets in a map
		pathsOfGraphs.put(graph.getName(), processedPath);
		
		LOG.exit();
	}

	@Override
	public boolean executeAlgorithmOnGraph(AlgorithmType algorithmType,
			Graph graph, Object parameters) {
		LOG.entry(algorithmType, graph, parameters);
		try {
			if (algorithmType == AlgorithmType.BFS) {
				jobCounter++;
				ToolRunner.run(new Configuration(),
						new BFSJob(pathsOfGraphs.get(graph.getName()),
								"/graphalytics-giraph/output/" + algorithmType + "-" + graph.getName(), parameters),
						new String[0]);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return LOG.exit(true);
	}

	@Override
	public void deleteGraph(String graphName) {
		// TODO Auto-generated method stub
		
	}

}
