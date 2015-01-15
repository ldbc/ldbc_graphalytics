package nl.tudelft.graphalytics;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import nl.tudelft.graphalytics.algorithms.AlgorithmType;
import nl.tudelft.graphalytics.algorithms.BFSParameters;
import nl.tudelft.graphalytics.algorithms.CDParameters;
import nl.tudelft.graphalytics.algorithms.EVOParameters;
import nl.tudelft.graphalytics.algorithms.STATSParameters;
import nl.tudelft.graphalytics.configuration.InvalidConfigurationException;
import nl.tudelft.graphalytics.configuration.ConfigurationUtil;
import nl.tudelft.graphalytics.reporting.BenchmarkReport;

public class BenchmarkSuite {
	private static final Logger log = LogManager.getLogger();
	
	private final String graphDirectoryPath;
	private final Map<BenchmarkConfiguration, BenchmarkRunResult> benchmarkRunResults = new HashMap<BenchmarkConfiguration, BenchmarkRunResult>();
	
	private Map<String, Graph> graphs = new HashMap<>();
	private Map<String, BenchmarkConfiguration[]> benchmarksPerGraph = new HashMap<>();
	
	private Set<String> graphSelection = new HashSet<>();
	private Set<String> algorithmSelection = new HashSet<>();
	
	private BenchmarkSuite(String graphDirectoryPath) {
		this.graphDirectoryPath = graphDirectoryPath;
	}
	
	private BenchmarkSuite withGraphs(Map<String, Graph> graphs) {
		this.graphs = graphs;
		return this;
	}
	
	private BenchmarkSuite withBenchmarksPerGraph(Map<String, BenchmarkConfiguration[]> benchmarksPerGraph) {
		this.benchmarksPerGraph = benchmarksPerGraph;
		return this;
	}
	
	private BenchmarkSuite withGraphSelection(Set<String> graphSelection) {
		this.graphSelection = graphSelection;
		return this;
	}
	
	private BenchmarkSuite withAlgorithmSelection(Set<String> algorithmSelection) {
		this.algorithmSelection = algorithmSelection;
		return this;
	}
	
	public void runOnPlatform(Platform platform) throws Exception {
		for (String graphName : graphs.keySet()) {
			if (!graphSelection.contains(graphName)) {
				log.debug("Skipping graph: " + graphName);
				continue;
			}
			
			Graph graph = graphs.get(graphName);
			platform.uploadGraph(graph, Paths.get(graphDirectoryPath, graph.getRelativeFilePath()).toString());
			for (BenchmarkConfiguration benchmarkRun : benchmarksPerGraph.get(graphName)) {
				if (!algorithmSelection.contains(benchmarkRun.getAlgorithmType().toString().toLowerCase())) {
					log.debug("Skipping algorithm: " + benchmarkRun.getAlgorithmType().toString().toLowerCase() +
							", on graph: " + graphName);
					continue;
				}
				
				BenchmarkRunResult result = benchmarkRun.executeOnPlatform(platform);
				if (!result.hasSucceeded())
					; // TODO: Handle properly
				
				benchmarkRunResults.put(benchmarkRun, result);
			}
			platform.deleteGraph(graph.getName());
		}
		
		BenchmarkReport report = BenchmarkReport.fromBenchmarkResults(benchmarkRunResults);
		report.generate("report-template/", platform.getName() + "-report/");
	}
	
	public static BenchmarkSuite readFromProperties() {
		try {
			Configuration graphConfiguration = new PropertiesConfiguration("benchmark.properties");
			
			// Get graph data directory
			String rootDir = ConfigurationUtil.getString(graphConfiguration, "graphs.root-directory");
			
			// Get list of available graphs
			String[] graphList = ConfigurationUtil.getStringArray(graphConfiguration, "graphs.names");
			
			// For each graph: read the general graph information and parse per-algorithm parameters
			Map<String, Graph> graphs = new HashMap<>();
			Map<String, BenchmarkConfiguration[]> benchmarkConfigs = new HashMap<>();
			for (String graphName : graphList) {
				Graph graphData = parseGraphConfiguration(graphConfiguration, "graph." + graphName, graphName);
				List<BenchmarkConfiguration> algorithms =
						parseGraphAlgorithms(graphConfiguration, "graph." + graphName, graphData);
				
				graphs.put(graphName, graphData);
				benchmarkConfigs.put(graphName, algorithms.toArray(new BenchmarkConfiguration[0]));
			}
			
			// Get graph selection, if any
			Set<String> graphSelection;
			String[] graphSelectionConfig = graphConfiguration.getStringArray("benchmark.run.graphs");
			if (graphSelectionConfig.length == 0 ||
					(graphSelectionConfig.length == 1 && graphSelectionConfig[0].trim().equals(""))) {
				graphSelection = graphs.keySet();
			} else {
				graphSelection = new HashSet<>(Arrays.asList(graphSelectionConfig));
			}
			// Get algorithm selection, if any
			Set<String> algorithmSelection;
			String[] algorithmSelectionConfig = graphConfiguration.getStringArray("benchmark.run.algorithms");
			if (algorithmSelectionConfig.length == 0 ||
					(algorithmSelectionConfig.length == 1 && algorithmSelectionConfig[0].trim().equals(""))) {
				algorithmSelection = new HashSet<>();
				for (AlgorithmType a : AlgorithmType.values())
					algorithmSelection.add(a.toString().toLowerCase());
			} else {
				algorithmSelection = new HashSet<>(Arrays.asList(algorithmSelectionConfig));
			}
			
			return new BenchmarkSuite(rootDir).
					withGraphs(graphs).
					withBenchmarksPerGraph(benchmarkConfigs).
					withGraphSelection(graphSelection).
					withAlgorithmSelection(algorithmSelection);
		} catch (ConfigurationException ex) {
			log.fatal("Failed to load benchmark configuration:");
			log.catching(Level.FATAL, ex);
			return null;
		} catch (InvalidConfigurationException ex) {
			log.fatal("Invalid benchmark configuration:");
			log.catching(Level.FATAL, ex);
			return null;
		}
	}
	
	private static Graph parseGraphConfiguration(Configuration config, String graphProperty, String graphName)
			throws InvalidConfigurationException {
		String fileName = ConfigurationUtil.getString(config, graphProperty + ".file");
		boolean isDirected = ConfigurationUtil.getBoolean(config, graphProperty + ".directed");
		boolean isEdgeBased = ConfigurationUtil.getBoolean(config, graphProperty + ".edge-based");
		return new Graph(graphName, fileName, isDirected, isEdgeBased);
	}
	
	private static List<BenchmarkConfiguration> parseGraphAlgorithms(Configuration config,
			String graphProperty, Graph graph) throws InvalidConfigurationException {
		List<BenchmarkConfiguration> res = new ArrayList<>();
		
		// Parse the list of algorithms available for this graph
		String[] algList = ConfigurationUtil.getStringArray(config, graphProperty + ".algorithms");
		
		// Go through the list of algorithms and parse their parameters
		for (String algorithm : algList) {
			switch (algorithm.toLowerCase()) {
			case "bfs":
				res.add(new BenchmarkConfiguration(
						AlgorithmType.BFS,
						graph,
						BFSParameters.fromConfiguration(config, graphProperty + ".bfs")
					));
				break;
			case "cd":
				res.add(new BenchmarkConfiguration(
						AlgorithmType.CD,
						graph,
						CDParameters.fromConfiguration(config, graphProperty + ".cd")
					));
				break;
			case "conn":
				res.add(new BenchmarkConfiguration(
						AlgorithmType.CONN,
						graph,
						null
					));
				break;
			case "evo":
				res.add(new BenchmarkConfiguration(
						AlgorithmType.EVO,
						graph,
						EVOParameters.fromConfiguration(config, graphProperty + ".evo")
					));
				break;
			case "stats":
				res.add(new BenchmarkConfiguration(
						AlgorithmType.STATS,
						graph,
						STATSParameters.fromConfiguration(config, graphProperty + ".stats")
					));
				break;
			default:
				log.warn("Unknown algorithm: \"" + algorithm + "\", specified in \"" + graphProperty +
						".algorithms\".");
				break;
			}
		}
		
		return res;
	}
}
