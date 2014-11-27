package org.tudelft.graphalytics.giraph;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ToolRunner;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tudelft.graphalytics.Graph;
import org.tudelft.graphalytics.Platform;
import org.tudelft.graphalytics.algorithms.AlgorithmType;
import org.tudelft.graphalytics.giraph.bfs.BFSJob;
import org.tudelft.graphalytics.giraph.conn.ConnectedComponentJob;
import org.tudelft.graphalytics.mapreduceutils.io.DirectedEdgeToVertexOutConversion;

public class GiraphPlatform implements Platform {
	private static final Logger LOG = LogManager.getLogger();

	private Map<String, String> pathsOfGraphs = new HashMap<>();
	
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

		// Prepare the appropriate job for the given algorithm type
		String inPath = pathsOfGraphs.get(graph.getName());
		String outPath = "/graphalytics-giraph/output/" + algorithmType + "-" + graph.getName();
		GiraphJob job;
		switch (algorithmType) {
		case BFS:
			job = new BFSJob(inPath, outPath, parameters);
			break;
		case CONN:
			job = new ConnectedComponentJob(inPath, outPath);
			break;
		default:
			LOG.warn("Unsupported algorithm: " + algorithmType);
			return LOG.exit(false);
		}
		
		// Execute the Giraph job
		try {
			int result = ToolRunner.run(new Configuration(), job, new String[0]);
			return LOG.exit(result == 0);
		} catch (Exception e) {
			LOG.catching(Level.ERROR, e);
			return LOG.exit(false);
		}
	}

	@Override
	public void deleteGraph(String graphName) {
		// TODO Auto-generated method stub
		
	}

}
