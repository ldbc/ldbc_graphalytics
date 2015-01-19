package nl.tudelft.graphalytics.mapreducev2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ToolRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import nl.tudelft.graphalytics.Graph;
import nl.tudelft.graphalytics.Platform;
import nl.tudelft.graphalytics.algorithms.AlgorithmType;
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
	
	private static final Map<AlgorithmType, Class<? extends MapReduceJobLauncher>> jobClassesPerAlgorithm = new HashMap<>();

	// Register the MapReduceJobLaunchers for all known algorithms
	{
		jobClassesPerAlgorithm.put(AlgorithmType.BFS, BreadthFirstSearchJobLauncher.class);
		jobClassesPerAlgorithm.put(AlgorithmType.CD, CommunityDetectionJobLauncher.class);
		jobClassesPerAlgorithm.put(AlgorithmType.CONN, ConnectedComponentsJobLauncher.class);
		jobClassesPerAlgorithm.put(AlgorithmType.EVO, ForestFireModelJobLauncher.class);
		jobClassesPerAlgorithm.put(AlgorithmType.STATS, STATSJobLauncher.class);
	}
	
	private Map<String, String> hdfsPathForGraphName = new HashMap<>();
	
	private org.apache.commons.configuration.Configuration mrConfig;

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
	}

	// TODO: Should the preprocessing be part of executeAlgorithmOnGraph?
	public void uploadGraph(Graph graph, String graphFilePath) throws IOException {
		log.entry(graph, graphFilePath);
		
		String hdfsPathRaw = "graphalytics-mapreducev2/input/raw-" + graph.getName();
		String hdfsPath = "graphalytics-mapreducev2/input/" + graph.getName();
		
		// Establish a connection with HDFS and upload the graph
		Configuration conf = new Configuration();
		FileSystem dfs = FileSystem.get(conf);
		dfs.copyFromLocalFile(new Path(graphFilePath), new Path(hdfsPathRaw));
		
		// If the graph needs to be preprocessed, do so, otherwise rename it
		if (graph.isEdgeBased()) {
			try {
				EdgesToAdjacencyListConversion job = new EdgesToAdjacencyListConversion(hdfsPathRaw, hdfsPath, graph.isDirected());
				if (mrConfig.containsKey("mapreducev2.reducer-count"))
					job.withNumberOfReducers(ConfigurationUtil.getInteger(mrConfig, "mapreducev2.reducer-count"));
				job.run();
			} catch (Exception e) {
				throw new IOException("Failed to preprocess graph: ", e);
			}
		} else if (graph.isDirected()) {
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

	public boolean executeAlgorithmOnGraph(AlgorithmType algorithmType, Graph graph, Object parameters) {
		log.entry(algorithmType, graph);
		try {
			MapReduceJobLauncher job = jobClassesPerAlgorithm.get(algorithmType).newInstance();
			job.parseGraphData(graph, parameters);
			job.setInputPath(hdfsPathForGraphName.get(graph.getName()));
			job.setIntermediatePath("graphalytics-mapreducev2/intermediate/" + algorithmType + "-" + graph.getName());
			job.setOutputPath("graphalytics-mapreducev2/output/" + algorithmType + "-" + graph.getName());
			
			// Set the number of reducers, if specified
			if (mrConfig.containsKey("mapreducev2.reducer-count"))
				job.setNumReducers(ConfigurationUtil.getInteger(mrConfig, "mapreducev2.reducer-count"));
			
			ToolRunner.run(new Configuration(), job, new String[0]);
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
	
	@Override
	public String getName() {
		return "mapreducev2";
	}

}