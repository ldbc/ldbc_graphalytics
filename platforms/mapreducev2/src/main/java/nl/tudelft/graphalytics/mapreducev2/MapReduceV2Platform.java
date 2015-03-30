package nl.tudelft.graphalytics.mapreducev2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nl.tudelft.graphalytics.PlatformExecutionException;
import nl.tudelft.graphalytics.domain.PlatformBenchmarkResult;
import nl.tudelft.graphalytics.domain.NestedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ToolRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import nl.tudelft.graphalytics.domain.Graph;
import nl.tudelft.graphalytics.Platform;
import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.configuration.ConfigurationUtil;
import nl.tudelft.graphalytics.mapreducev2.bfs.BreadthFirstSearchJobLauncher;
import nl.tudelft.graphalytics.mapreducev2.cd.CommunityDetectionJobLauncher;
import nl.tudelft.graphalytics.mapreducev2.conn.ConnectedComponentsJobLauncher;
import nl.tudelft.graphalytics.mapreducev2.conversion.DirectedVertexToAdjacencyListConversion;
import nl.tudelft.graphalytics.mapreducev2.conversion.EdgesToAdjacencyListConversion;
import nl.tudelft.graphalytics.mapreducev2.evo.ForestFireModelJobLauncher;
import nl.tudelft.graphalytics.mapreducev2.stats.STATSJobLauncher;

/**
 * Graphalytics Platform implementation for the MapReduce v2 platform. Manages
 * datasets on HDFS and launches MapReduce jobs to run algorithms on these
 * datasets.
 *
 * @author Tim Hegeman
 */
public class MapReduceV2Platform implements Platform {
	private static final Logger log = LogManager.getLogger();
	
	private static final Map<Algorithm, Class<? extends MapReduceJobLauncher>> jobClassesPerAlgorithm = new HashMap<>();

	// Register the MapReduceJobLaunchers for all known algorithms
	{
		jobClassesPerAlgorithm.put(Algorithm.BFS, BreadthFirstSearchJobLauncher.class);
		jobClassesPerAlgorithm.put(Algorithm.CD, CommunityDetectionJobLauncher.class);
		jobClassesPerAlgorithm.put(Algorithm.CONN, ConnectedComponentsJobLauncher.class);
		jobClassesPerAlgorithm.put(Algorithm.EVO, ForestFireModelJobLauncher.class);
		jobClassesPerAlgorithm.put(Algorithm.STATS, STATSJobLauncher.class);
	}

	/** Property key for the directory on HDFS in which to store all input and output. */
	public static final String HDFS_DIRECTORY_KEY = "hadoop.hdfs.directory";
	/** Default value for the directory on HDFS in which to store all input and output. */
	public static final String HDFS_DIRECTORY = "graphalytics";
	
	private Map<String, String> hdfsPathForGraphName = new HashMap<>();
	
	private org.apache.commons.configuration.Configuration mrConfig;

	private String hdfsDirectory;

	/**
	 * Initialises the platform driver by reading the platform-specific properties file.
	 */
	public MapReduceV2Platform() {
		try {
			mrConfig = new PropertiesConfiguration("mapreducev2.properties");
		} catch (ConfigurationException e) {
			log.warn("Could not find or load mapreducev2.properties.");
			mrConfig = new PropertiesConfiguration();
		}
		hdfsDirectory = mrConfig.getString(HDFS_DIRECTORY_KEY, HDFS_DIRECTORY);
	}

	// TODO: Should the preprocessing be part of executeAlgorithmOnGraph?
	public void uploadGraph(Graph graph, String graphFilePath) throws IOException {
		log.entry(graph, graphFilePath);
		
		String hdfsPathRaw = hdfsDirectory + "/mapreducev2/input/raw-" + graph.getName();
		String hdfsPath = hdfsDirectory + "/mapreducev2/input/" + graph.getName();
		
		// Establish a connection with HDFS and upload the graph
		Configuration conf = new Configuration();
		FileSystem dfs = FileSystem.get(conf);
		dfs.copyFromLocalFile(new Path(graphFilePath), new Path(hdfsPathRaw));
		
		// If the graph needs to be preprocessed, do so, otherwise rename it
		if (graph.getGraphFormat().isEdgeBased()) {
			try {
				EdgesToAdjacencyListConversion job = new EdgesToAdjacencyListConversion(hdfsPathRaw, hdfsPath, graph.getGraphFormat().isDirected());
				if (mrConfig.containsKey("mapreducev2.reducer-count"))
					job.withNumberOfReducers(ConfigurationUtil.getInteger(mrConfig, "mapreducev2.reducer-count"));
				job.run();
			} catch (Exception e) {
				throw new IOException("Failed to preprocess graph: ", e);
			}
		} else if (graph.getGraphFormat().isDirected()) {
			try {
				DirectedVertexToAdjacencyListConversion job =
						new DirectedVertexToAdjacencyListConversion(hdfsPathRaw, hdfsPath);
				if (mrConfig.containsKey("mapreducev2.reducer-count"))
					job.withNumberOfReducers(ConfigurationUtil.getInteger(mrConfig, "mapreducev2.reducer-count"));
				job.run();
			} catch (Exception e) {
				throw new IOException("Failed to preprocess graph: ", e);
			}
		} else {
			// Rename the graph
			dfs.rename(new Path(hdfsPathRaw), new Path(hdfsPath));
		}
		
		hdfsPathForGraphName.put(graph.getName(), hdfsPath);
		log.exit();
	}

	public PlatformBenchmarkResult executeAlgorithmOnGraph(Algorithm algorithm, Graph graph, Object parameters)
			throws PlatformExecutionException {
		log.entry(algorithm, graph);
		int result;
		try {
			MapReduceJobLauncher job = jobClassesPerAlgorithm.get(algorithm).newInstance();
			job.parseGraphData(graph, parameters);
			job.setInputPath(hdfsPathForGraphName.get(graph.getName()));
			job.setIntermediatePath(hdfsDirectory + "/mapreducev2/intermediate/" + algorithm + "-" + graph.getName());
			job.setOutputPath(hdfsDirectory + "/mapreducev2/output/" + algorithm + "-" + graph.getName());
			
			// Set the number of reducers, if specified
			if (mrConfig.containsKey("mapreducev2.reducer-count"))
				job.setNumReducers(ConfigurationUtil.getInteger(mrConfig, "mapreducev2.reducer-count"));
			
			result = ToolRunner.run(new Configuration(), job, new String[0]);
		} catch (Exception e) {
			throw new PlatformExecutionException("MapReduce job failed with exception: ", e);
		}

		if (result != 0)
			throw new PlatformExecutionException("MapReduce job completed with exit code = " + result);
		return new PlatformBenchmarkResult(NestedConfiguration.empty());
	}

	public void deleteGraph(String graphName) {
		// TODO Auto-generated method stub
		log.entry(graphName);

		log.exit();
	}
	
	@Override
	public String getName() {
		return "mapreducev2";
	}

	@Override
	public NestedConfiguration getPlatformConfiguration() {
		try {
			org.apache.commons.configuration.Configuration configuration =
					new PropertiesConfiguration("mapreducev2.properties");
			return NestedConfiguration.fromExternalConfiguration(configuration, "mapreducev2.properties");
		} catch (ConfigurationException ex) {
			return NestedConfiguration.empty();
		}
	}

}
