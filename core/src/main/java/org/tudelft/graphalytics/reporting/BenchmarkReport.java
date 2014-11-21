package org.tudelft.graphalytics.reporting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.tudelft.graphalytics.BenchmarkConfiguration;
import org.tudelft.graphalytics.BenchmarkRunResult;

public class BenchmarkReport {

	private Map<String, Graph> graphs;
	private Map<String, Algorithm> algorithms;
	
	private BenchmarkReport(Map<String, Graph> graphs, Map<String, Algorithm> algorithms) {
		this.graphs = graphs;
		this.algorithms = algorithms;
	}
	
	public Collection<Graph> getGraphs() {
		return graphs.values();
	}
	
	public Collection<Algorithm> getAlgorithms() {
		return algorithms.values();
	}
	
	public void generate(String templateFolder, String outputFolder) throws IOException {
		TemplateEngine engine = new TemplateEngine(FileUtils.getFile(templateFolder).getAbsolutePath());
		
		Path reportDir = Files.createTempDirectory("graphalytics-report-");
		FileUtils.copyDirectory(FileUtils.getFile(templateFolder), reportDir.toFile());
		
		engine.putVariable("report", this);
		String indexHtml = engine.processTemplate("index");
		File indexHtmlFile = reportDir.resolve("index.html").toFile();
		indexHtmlFile.createNewFile();
		FileUtils.writeStringToFile(indexHtmlFile, indexHtml);
		
		if (FileUtils.getFile(outputFolder).exists())
			throw new IOException("Output \"" + outputFolder + "\" already exists!\n" +
					"Report can be found in \"" + reportDir.toAbsolutePath() + "\"");
		FileUtils.moveDirectory(reportDir.toFile(), FileUtils.getFile(outputFolder));
	}
	
	public static BenchmarkReport fromBenchmarkResults(Map<BenchmarkConfiguration, BenchmarkRunResult> results) {
		Map<String, Graph> graphs = new HashMap<>();
		Map<String, Algorithm> algorithms = new HashMap<>();
		
		for (Map.Entry<BenchmarkConfiguration, BenchmarkRunResult> result : results.entrySet()) {
			String algorithm = result.getKey().getAlgorithmType().toString();
			if (!algorithms.containsKey(algorithm))
				algorithms.put(algorithm, new Algorithm(algorithm));
			
			String graph = result.getKey().getGraph().getName();
			if (!graphs.containsKey(graph))
				graphs.put(graph, new Graph(graph));
			
			Result res = new Result(algorithms.get(algorithm), graphs.get(graph));
			res.setSucceeded(result.getValue().hasSucceeded());
			res.setRuntimeMs(result.getValue().getElapsedTimeInMillis());
			
			graphs.get(graph).addResult(algorithm, res);
			algorithms.get(algorithm).addResult(graph, res);
		}
		
		return new BenchmarkReport(graphs, algorithms);
	}
	
	@Deprecated
	public static BenchmarkReport fromTestData(Map<String, Graph> graphs, Map<String, Algorithm> algorithms) {
		return new BenchmarkReport(graphs, algorithms);
	}
	
}
