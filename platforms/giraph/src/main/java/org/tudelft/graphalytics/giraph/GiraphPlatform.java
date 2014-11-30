package org.tudelft.graphalytics.giraph;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
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
import org.tudelft.graphalytics.configuration.ConfigurationUtil;
import org.tudelft.graphalytics.giraph.bfs.BFSJob;
import org.tudelft.graphalytics.giraph.cd.CommunityDetectionJob;
import org.tudelft.graphalytics.giraph.conn.ConnectedComponentJob;
import org.tudelft.graphalytics.giraph.evo.ForestFireModelJob;
import org.tudelft.graphalytics.giraph.stats.StatsJob;
import org.tudelft.graphalytics.giraph.conversion.EdgesToAdjacencyListConversion;

public class GiraphPlatform implements Platform {
	private static final Logger LOG = LogManager.getLogger();

	public static final String PREPROCESSING_NUMREDUCERS = "giraph.preprocessing.num-reducers";
	public static final String JOB_WORKERCOUNT = "giraph.job.worker-count";
	public static final String JOB_HEAPSIZE = "giraph.job.heap-size";
	public static final String ZOOKEEPERADDRESS = "giraph.zoo-keeper-address";
	
	private Map<String, String> pathsOfGraphs = new HashMap<>();
	private org.apache.commons.configuration.Configuration giraphConfig;
//	private org.apache.commons.configuration.Configuration yarnConfig;
	
	public GiraphPlatform() {
		loadConfiguration();
	}
	
	private void loadConfiguration() {
		// Load YARN-specific configuration
//		try {
//			yarnConfig = new PropertiesConfiguration("yarn.properties");
//		} catch (ConfigurationException e) {
//			LOG.info("Could not find or load yarn.properties.");
//			yarnConfig = new PropertiesConfiguration();
//		}
		
		// Load Giraph-specific configuration
		try {
			giraphConfig = new PropertiesConfiguration("giraph.properties");
		} catch (ConfigurationException e) {
			LOG.info("Could not find or load giraph.properties.");
			giraphConfig = new PropertiesConfiguration();
		}
	}
	
	@Override
	public void uploadGraph(Graph graph, String graphFilePath) throws Exception {
		LOG.entry(graph, graphFilePath);
		
		String tempPath = "/graphalytics-giraph/raw-input/" + graph.getName();
		String processedPath = "/graphalytics-giraph/input/" + graph.getName();
		
		// Upload the raw data to HDFS
		FileSystem fs = FileSystem.get(new Configuration());
		fs.copyFromLocalFile(new Path(graphFilePath), new Path(tempPath));
		
		// Preprocess the graph to an adjacency list format
		if (graph.isEdgeBased()) {
			EdgesToAdjacencyListConversion conversion =
					new EdgesToAdjacencyListConversion(tempPath, processedPath, graph.isDirected());
			if (giraphConfig.containsKey(PREPROCESSING_NUMREDUCERS))
				conversion.withNumberOfReducers(
						ConfigurationUtil.getInteger(giraphConfig, PREPROCESSING_NUMREDUCERS));
			conversion.run();
			// Remove the raw data
			fs.delete(new Path(tempPath), true);
		} else {
			// The edge-based format is what is used by the Giraph jobs,
			// so just rename the raw data to be the processed data
			fs.rename(new Path(tempPath), new Path(processedPath));
		}
		
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
			// Prepare the appropriate job for the given algorithm type
			String inPath = pathsOfGraphs.get(graph.getName());
			String outPath = "/graphalytics-giraph/output/" + algorithmType + "-" + graph.getName();
			String zooKeeperAddress = ConfigurationUtil.getString(giraphConfig, ZOOKEEPERADDRESS);
			GiraphJob job;
			switch (algorithmType) {
			case BFS:
				job = new BFSJob(inPath, outPath, zooKeeperAddress, parameters);
				break;
			case CD:
				job = new CommunityDetectionJob(inPath, outPath, zooKeeperAddress, parameters, graph.isDirected());
				break;
			case CONN:
				job = new ConnectedComponentJob(inPath, outPath, zooKeeperAddress);
				break;
			case EVO:
				job = new ForestFireModelJob(inPath, outPath, zooKeeperAddress, parameters, graph.isDirected());
				break;
			case STATS:
				job = new StatsJob(inPath, outPath, zooKeeperAddress, parameters, graph.isDirected());
				break;
			default:
				LOG.warn("Unsupported algorithm: " + algorithmType);
				return LOG.exit(false);
			}
			
			// Configure the job using the Giraph properties file
			if (giraphConfig.containsKey(JOB_WORKERCOUNT))
				job.setWorkerCount(ConfigurationUtil.getInteger(giraphConfig, JOB_WORKERCOUNT));
			else
				LOG.warn("Worker count is not configured, defaulting to 1. Set \"" +
						JOB_WORKERCOUNT + "\" for better results.");
			if (giraphConfig.containsKey(JOB_HEAPSIZE))
				job.setHeapSize(ConfigurationUtil.getInteger(giraphConfig, JOB_HEAPSIZE));
			else
				LOG.warn("Heap size is not configured, deafulting to 1024. Set \"" +
						JOB_HEAPSIZE + "\" for better results.");
			
			// Execute the Giraph job
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
