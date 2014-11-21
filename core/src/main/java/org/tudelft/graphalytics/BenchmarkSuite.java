package org.tudelft.graphalytics;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tudelft.graphalytics.algorithms.AlgorithmType;
import org.tudelft.graphalytics.algorithms.BFSParameters;
import org.tudelft.graphalytics.algorithms.CDParameters;
import org.tudelft.graphalytics.algorithms.EVOParameters;
import org.tudelft.graphalytics.configuration.InvalidConfigurationException;
import org.tudelft.graphalytics.configuration.ConfigurationUtil;
import org.tudelft.graphalytics.reporting.BenchmarkReport;

public class BenchmarkSuite {
	private static final Logger log = LogManager.getLogger();
	
//	private static final Map<String, Graph> graphs = new HashMap<>(); 
//	
//	private static final Map<String, BenchmarkConfiguration[]> benchmarksPerGraph = new HashMap<>();
//	
//	private static void registerGraph(Graph graph, AlgorithmType[] algorithms, Object[] parameters) {
//		graphs.put(graph.getName(), graph);
//		
//		assert(algorithms.length == parameters.length);
//		
//		BenchmarkConfiguration[] benchmarks = new BenchmarkConfiguration[algorithms.length];
//		for (int i = 0; i < algorithms.length; i++)
//			benchmarks[i] = new BenchmarkConfiguration(algorithms[i], graph, parameters[i]);
//		benchmarksPerGraph.put(graph.getName(), benchmarks);
//	}
	
//	{
//		registerGraph(
//				new Graph("LDBC-Scale-1", "ldbc-snb/SF-1/person_knows_person.graphDE", true, true),
//				new AlgorithmType[] {
//					AlgorithmType.BFS,
//					AlgorithmType.CD,
//					AlgorithmType.CONN,
//					AlgorithmType.EVO
//				},
//				new Object[] {
//					new BFSParameters("12094627913375"),
//					new CDParameters(0.1f, 0.1f, 10),
//					null,
//					new EVOParameters(18691699072470L, 0.5f, 0.5f, 6, 5)
//				});
//		registerGraph(
//				new Graph("LDBC-Scale-3", "ldbc-snb/SF-3/person_knows_person.graphDE", true, true),
//				new AlgorithmType[] {
//					AlgorithmType.BFS,
//					AlgorithmType.CD,
//					AlgorithmType.CONN,
//					AlgorithmType.EVO
//				},
//				new Object[] {
//					new BFSParameters("12094627913375"),
//					new CDParameters(0.1f, 0.1f, 10),
//					null,
//					new EVOParameters(18691701125666L, 0.5f, 0.5f, 6, 5)
//				});
//		registerGraph(
//				new Graph("LDBC-Scale-10", "ldbc-snb/SF-10/person_knows_person.graphDE", true, true),
//				new AlgorithmType[] {
//					AlgorithmType.BFS,
//					AlgorithmType.CD,
//					AlgorithmType.CONN,
//					AlgorithmType.EVO
//				},
//				new Object[] {
//					new BFSParameters("12094627913375"),
//					new CDParameters(0.1f, 0.1f, 10),
//					null,
//					new EVOParameters(18691707014993L, 0.5f, 0.5f, 6, 5)
//				});
//	}
	
	private final String graphDirectoryPath;
	private final Map<BenchmarkConfiguration, BenchmarkRunResult> benchmarkRunResults = new HashMap<BenchmarkConfiguration, BenchmarkRunResult>();
	
	private Map<String, Graph> graphs = new HashMap<>();
	private Map<String, BenchmarkConfiguration[]> benchmarksPerGraph = new HashMap<>();
	
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
	
	public void runOnPlatform(Platform platform) throws IOException {
		for (String graphName : graphs.keySet()) {
			Graph graph = graphs.get(graphName);
			platform.uploadGraph(graphName, Paths.get(graphDirectoryPath, graph.getRelativeFilePath()).toString());
			for (BenchmarkConfiguration benchmarkRun : benchmarksPerGraph.get(graphName)) {
				// TODO: Implement way to select subset of graphs/algorithms
				//if (benchmarkRun.getAlgorithmType() != AlgorithmType.EVO)
				//	continue;
				
				BenchmarkRunResult result = benchmarkRun.executeOnPlatform(platform);
				if (!result.hasSucceeded())
					return; // TODO: Handle properly
				
				benchmarkRunResults.put(benchmarkRun, result);
			}
			platform.deleteGraph(graph.getName());
		}
		
//		System.out.println();
//		System.out.println();
//		System.out.println("Benchmark results:");
//		System.out.println();
//		for (Map.Entry<BenchmarkConfiguration, BenchmarkRunResult> result : benchmarkRunResults.entrySet()) {
//			AlgorithmType alg = result.getKey().getAlgorithmType();
//			String graph = result.getKey().getGraph().getName();
//			long millis = result.getValue().getElapsedTimeInMillis();
//			double seconds = millis / 1000.0;
//			System.out.println("Completed " + alg.toString() + " on " + graph + " in " + seconds + " seconds.");
//		}
//		System.out.println();
		BenchmarkReport report = BenchmarkReport.fromBenchmarkResults(benchmarkRunResults);
		report.generate("report-template/", "sample-report/");
	}
	
	public static BenchmarkSuite readFromProperties() {
		try {
			Configuration graphConfiguration = new PropertiesConfiguration("graphs.properties");
			
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
			
			return new BenchmarkSuite(rootDir).withGraphs(graphs).withBenchmarksPerGraph(benchmarkConfigs);
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
			default:
				log.warn("Unknown algorithm: \"" + algorithm + "\", specified in \"" + graphProperty +
						".algorithms\".");
				break;
			}
		}
		
		return res;
	}
}
