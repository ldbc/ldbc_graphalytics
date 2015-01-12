package org.tudelft.graphalytics.giraph;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.giraph.conf.IntConfOption;
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
import org.tudelft.graphalytics.configuration.InvalidConfigurationException;
import org.tudelft.graphalytics.giraph.bfs.BreadthFirstSearchJob;
import org.tudelft.graphalytics.giraph.cd.CommunityDetectionJob;
import org.tudelft.graphalytics.giraph.conn.ConnectedComponentsJob;
import org.tudelft.graphalytics.giraph.evo.ForestFireModelJob;
import org.tudelft.graphalytics.giraph.stats.LocalClusteringCoefficientJob;

/**
 * Entry point of the Graphalytics benchmark for Giraph. Provides the platform
 * API required by the Graphalytics core to perform operations such as uploading
 * graphs and executing specific algorithms on specific graphs.
 * 
 * @author Tim Hegeman
 */
public class GiraphPlatform implements Platform {
	private static final Logger LOG = LogManager.getLogger();

	/** Property key for setting the number of workers to be used for running Giraph jobs. */
	public static final String JOB_WORKERCOUNT = "giraph.job.worker-count";
	/** Property key for setting the heap size of each Giraph worker. */
	public static final String JOB_HEAPSIZE = "giraph.job.heap-size";
	/** Property key for setting the memory size of each Giraph worker. */
	public static final String JOB_MEMORYSIZE = "giraph.job.memory-size";
	/** Property key for the address of a ZooKeeper instance to use during the benchmark. */ 
	public static final String ZOOKEEPERADDRESS = "giraph.zoo-keeper-address";
	
	// TODO: Make configurable
	private static final String BASE_ADDRESS = "graphalytics-giraph";
	
	private Map<String, String> pathsOfGraphs = new HashMap<>();
	private org.apache.commons.configuration.Configuration giraphConfig;

	/**
	 * Constructor that opens the Giraph-specific properties file for the public
	 * API implementation to use.
	 */
	public GiraphPlatform() {
		loadConfiguration();
	}
	
	private void loadConfiguration() {
		// Load Giraph-specific configuration
		try {
			giraphConfig = new PropertiesConfiguration("giraph.properties");
		} catch (ConfigurationException e) {
			// Fall-back to an empty properties file
			LOG.info("Could not find or load giraph.properties.");
			giraphConfig = new PropertiesConfiguration();
		}
	}
	
	@Override
	public void uploadGraph(Graph graph, String graphFilePath) throws Exception {
		LOG.entry(graph, graphFilePath);
		
		String uploadPath = BASE_ADDRESS + "/input/" + graph.getName();
		
		// Upload the graph to HDFS
		FileSystem fs = FileSystem.get(new Configuration());
		fs.copyFromLocalFile(new Path(graphFilePath), new Path(uploadPath));
		fs.close();
		
		// Track available datasets in a map
		pathsOfGraphs.put(graph.getName(), uploadPath);
		
		LOG.exit();
	}

	@Override
	public boolean executeAlgorithmOnGraph(AlgorithmType algorithmType,
			Graph graph, Object parameters) {
		LOG.entry(algorithmType, graph, parameters);

		try {
			// Prepare the appropriate job for the given algorithm type
			GiraphJob job;
			switch (algorithmType) {
			case BFS:
				job = new BreadthFirstSearchJob(parameters, graph.getGraphFormat());
				break;
			case CD:
				job = new CommunityDetectionJob(parameters, graph.getGraphFormat());
				break;
			case CONN:
				job = new ConnectedComponentsJob(graph.getGraphFormat());
				break;
			case EVO:
				job = new ForestFireModelJob(parameters, graph.getGraphFormat());
				break;
			case STATS:
				job = new LocalClusteringCoefficientJob(graph.getGraphFormat());
				break;
			default:
				LOG.warn("Unsupported algorithm: " + algorithmType);
				return LOG.exit(false);
			}
			
			// Create the job configuration using the Giraph properties file
			Configuration jobConf = new Configuration();
			GiraphJob.INPUT_PATH.set(jobConf, pathsOfGraphs.get(graph.getName()));
			GiraphJob.OUTPUT_PATH.set(jobConf, BASE_ADDRESS + "/output/" + algorithmType + "-" + graph.getName());
			GiraphJob.ZOOKEEPER_ADDRESS.set(jobConf, ConfigurationUtil.getString(giraphConfig, ZOOKEEPERADDRESS));
			transferIfSet(giraphConfig, JOB_WORKERCOUNT, jobConf, GiraphJob.WORKER_COUNT);
			transferIfSet(giraphConfig, JOB_HEAPSIZE, jobConf, GiraphJob.HEAP_SIZE_MB);
			transferIfSet(giraphConfig, JOB_MEMORYSIZE, jobConf, GiraphJob.WORKER_MEMORY_MB);
			
			// Execute the Giraph job
			int result = ToolRunner.run(jobConf, job, new String[0]);
			return LOG.exit(result == 0);
		} catch (Exception e) {
			LOG.catching(Level.ERROR, e);
			return LOG.exit(false);
		}
	}
	
	private void transferIfSet(org.apache.commons.configuration.Configuration source, String sourceProperty,
			Configuration destination, IntConfOption destinationOption) throws InvalidConfigurationException {
		if (source.containsKey(sourceProperty))
			destinationOption.set(destination, ConfigurationUtil.getInteger(source, sourceProperty));
		else
			LOG.warn(sourceProperty + " is not configured, defaulting to " +
					destinationOption.getDefaultValue() + ".");
	}

	@Override
	public void deleteGraph(String graphName) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public String getName() {
		return "giraph";
	}

}
