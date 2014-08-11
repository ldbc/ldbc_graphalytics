package org.tudelft.graphalytics;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class BenchmarkSuite {

	private static final Graph[] graphs = {
		new Graph("Amazon", ""),
		new Graph("WikiTalk", ""),
		new Graph("KGS", ""),
		new Graph("Citation", ""),
		new Graph("DotaLeague", ""),
		new Graph("Friendster", "")
	};
	
	private final String graphDirectoryPath;
	private final Map<BenchmarkRun, BenchmarkRunResult> benchmarkRunResults = new HashMap<BenchmarkRun, BenchmarkRunResult>();
	
	public BenchmarkSuite(String graphDirectoryPath) {
		this.graphDirectoryPath = graphDirectoryPath;
	}
	
	public void runOnPlatform(Platform platform) {
		for (Graph graph : graphs) {
			platform.uploadGraph(graph.getName(), Paths.get(graphDirectoryPath, graph.getRelativeFilePath()).toString());
			for (AlgorithmType algorithmType : AlgorithmType.values()) {
				BenchmarkRun benchmarkRun = new BenchmarkRun(algorithmType, graph);
				BenchmarkRunResult result = benchmarkRun.executeOnPlatform(platform);
				if (!result.hasSucceeded())
					return; // TODO: Handle properly
				
				benchmarkRunResults.put(benchmarkRun, result);
			}
			platform.deleteGraph(graph.getName());
		}
	}
	
}
