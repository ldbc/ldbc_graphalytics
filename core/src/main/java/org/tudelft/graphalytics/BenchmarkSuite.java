package org.tudelft.graphalytics;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.tudelft.graphalytics.algorithms.AlgorithmType;
import org.tudelft.graphalytics.algorithms.BFSParameters;
import org.tudelft.graphalytics.algorithms.CDParameters;

public class BenchmarkSuite {

	private static final Map<String, Graph> graphs = new HashMap<>(); 
		{
//		new Graph("Amazon", ""),
//		new Graph("WikiTalk", ""),
//		new Graph("KGS", ""),
//		new Graph("Citation", ""),
//		new Graph("DotaLeague", ""),
//		new Graph("Friendster", "")
		
	};
	
	private static final Map<String, BenchmarkConfiguration[]> benchmarksPerGraph = new HashMap<>();
	
	private static void registerGraph(Graph graph, AlgorithmType[] algorithms, Object[] parameters) {
		graphs.put(graph.getName(), graph);
		
		assert(algorithms.length == parameters.length);
		
		BenchmarkConfiguration[] benchmarks = new BenchmarkConfiguration[algorithms.length];
		for (int i = 0; i < algorithms.length; i++)
			benchmarks[i] = new BenchmarkConfiguration(algorithms[i], graph, parameters[i]);
		benchmarksPerGraph.put(graph.getName(), benchmarks);
	}
	
	{
		registerGraph(
				new Graph("LDBC-Scale-1", "ldbc-snb/SF-1/person_knows_person.graphDE", true, true),
				new AlgorithmType[] {
					AlgorithmType.BFS,
					AlgorithmType.CD,
					AlgorithmType.CONN
				},
				new Object[] {
					new BFSParameters("12094627913375"),
					new CDParameters(0.1f, 0.1f, 10),
					null
				});
		registerGraph(
				new Graph("LDBC-Scale-3", "ldbc-snb/SF-3/person_knows_person.graphDE", true, true),
				new AlgorithmType[] {
					AlgorithmType.BFS,
					AlgorithmType.CD,
					AlgorithmType.CONN
				},
				new Object[] {
					new BFSParameters("12094627913375"),
					new CDParameters(0.1f, 0.1f, 10),
					null
				});
		registerGraph(
				new Graph("LDBC-Scale-10", "ldbc-snb/SF-10/person_knows_person.graphDE", true, true),
				new AlgorithmType[] {
					AlgorithmType.BFS,
					AlgorithmType.CD,
					AlgorithmType.CONN
				},
				new Object[] {
					new BFSParameters("12094627913375"),
					new CDParameters(0.1f, 0.1f, 10),
					null
				});
	}
	
	private final String graphDirectoryPath;
	private final Map<BenchmarkConfiguration, BenchmarkRunResult> benchmarkRunResults = new HashMap<BenchmarkConfiguration, BenchmarkRunResult>();
	
	public BenchmarkSuite(String graphDirectoryPath) {
		this.graphDirectoryPath = graphDirectoryPath;
	}
	
	public void runOnPlatform(Platform platform) throws IOException {
		for (String graphName : graphs.keySet()) {
			Graph graph = graphs.get(graphName);
			platform.uploadGraph(graphName, Paths.get(graphDirectoryPath, graph.getRelativeFilePath()).toString());
			for (BenchmarkConfiguration benchmarkRun : benchmarksPerGraph.get(graphName)) {
				// TODO: Implement way to select subset of graphs/algorithms
				if (benchmarkRun.getAlgorithmType() != AlgorithmType.CONN)
					continue;
				
				BenchmarkRunResult result = benchmarkRun.executeOnPlatform(platform);
				if (!result.hasSucceeded())
					return; // TODO: Handle properly
				
				benchmarkRunResults.put(benchmarkRun, result);
			}
			platform.deleteGraph(graph.getName());
		}
		
		System.out.println();
		System.out.println();
		System.out.println("Benchmark results:");
		System.out.println();
		for (Map.Entry<BenchmarkConfiguration, BenchmarkRunResult> result : benchmarkRunResults.entrySet()) {
			AlgorithmType alg = result.getKey().getAlgorithmType();
			String graph = result.getKey().getGraph().getName();
			long millis = result.getValue().getElapsedTimeInMillis();
			double seconds = millis / 1000.0;
			System.out.println("Completed " + alg.toString() + " on " + graph + " in " + seconds + " seconds.");
		}
		System.out.println();
	}
}
