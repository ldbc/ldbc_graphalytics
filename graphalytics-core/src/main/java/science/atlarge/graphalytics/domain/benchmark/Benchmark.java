/*
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package science.atlarge.graphalytics.domain.benchmark;

import science.atlarge.graphalytics.domain.algorithms.Algorithm;
import science.atlarge.graphalytics.domain.algorithms.AlgorithmParameters;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import science.atlarge.graphalytics.domain.graph.StandardGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The Graphalytics benchmark suite; a collection of benchmarks using multiple algorithms and running on multiple
 * graphs. The exact algorithms and graphs that are part of this suite are controlled by external configuration
 * files.
 *
 * @author Tim Hegeman
 */
public class Benchmark implements Serializable {

	private static final Logger LOG = LogManager.getLogger();

	protected String platformName;
	protected String type;

	protected int timeout;
	protected boolean outputRequired;
	protected boolean validationRequired;

	protected Path baseLogDir;
	protected Path baseOutputDir;
	protected Path baseValidationDir;

	protected Collection<BenchmarkExp> experiments;
	protected Collection<BenchmarkJob> jobs;
	protected Collection<BenchmarkRun> benchmarkRuns;
	protected Set<Algorithm> algorithms;
	protected Set<Graph> graphs;

	protected Map<String, Graph> foundGraphs;
	protected Map<String, Map<Algorithm, AlgorithmParameters>> algorithmParameters;

	public Benchmark(String platformName, int timeout, boolean outputRequired, boolean validationRequired,
					 Path baseLogDir, Path baseOutputDir, Path baseValidationDir,
					 Map<String, Graph> foundGraphs, Map<String, Map<Algorithm, AlgorithmParameters>> algorithmParameters) {

		this.platformName = platformName;

		this.timeout = timeout;
		this.outputRequired = outputRequired;
		this.validationRequired = validationRequired;

		this.baseLogDir = baseLogDir;
		this.baseOutputDir = baseOutputDir;
		this.baseValidationDir = baseValidationDir;

		this.foundGraphs = foundGraphs;
		this.algorithmParameters = algorithmParameters;

		experiments = new ArrayList<>();
		jobs = new ArrayList<>();
		benchmarkRuns = new ArrayList<>();
		algorithms = new HashSet<>();
		graphs = new HashSet<>();
	}

	public Benchmark(Collection<BenchmarkExp> experiments, Collection<BenchmarkJob> jobs,
					 Collection<BenchmarkRun> benchmarkRuns, Set<Algorithm> algorithms,
					 Set<Graph> graphs, Path baseLogDir) {
		this.experiments = experiments;
		this.jobs = jobs;
		this.benchmarkRuns = benchmarkRuns;
		this.algorithms = algorithms;
		this.graphs = graphs;
		this.baseLogDir = baseLogDir;
	}

	protected BenchmarkRun contructBenchmarkRun(Algorithm algorithm, Graph graph) {
		if (graph == null) {
			LOG.error(String.format("Required graphset not available. Note that error should have already been caught earlier."));
			throw new IllegalStateException("Loading failed: benchmark cannot be constructed due to missing graphs.");
		}

		return new BenchmarkRun(algorithm, graph,
				timeout, outputRequired, validationRequired,
				baseLogDir, baseOutputDir, baseValidationDir);
	}


	public Collection<BenchmarkExp> getExperiments() {
		return experiments;
	}

	public Collection<BenchmarkJob> getJobs() {
		return jobs;
	}

	/**
	 * @return the benchmarks that make up the Graphalytics benchmark suite
	 */
	public Collection<BenchmarkRun> getBenchmarkRuns() {
		return Collections.unmodifiableCollection(benchmarkRuns);
	}

	/**
	 * @return the set of algorithms used in the Graphalytics benchmark suite
	 */
	public Set<Algorithm> getAlgorithms() {
		return Collections.unmodifiableSet(algorithms);
	}

	/**
	 * @return the set of graphs used in the Graphalytics benchmark suite
	 */
	public Set<Graph> getGraphs() {
		return Collections.unmodifiableSet(graphs);
	}

	/**
	 * @param formattedGraph the graph for which to retrieve all benchmarks
	 * @return the subset of benchmarks running on the specified graph
	 */
	public Collection<BenchmarkRun> getBenchmarksForGraph(FormattedGraph formattedGraph) {
		Collection<BenchmarkRun> benchmarksForGraph = new ArrayList<>();
		for (BenchmarkRun benchmarkRun : benchmarkRuns) {
			if (benchmarkRun.getFormattedGraph().equals(formattedGraph)) {
				benchmarksForGraph.add(benchmarkRun);
			}
		}
		return benchmarksForGraph;
	}

	public Path getBaseLogDir() {
		return baseLogDir;
	}

	protected static String formatReportDirectory(String platformName, String benchmarkType) {
		String timestamp = new SimpleDateFormat("yyMMdd-HHmmss").format(Calendar.getInstance().getTime());
		String outputDirectoryPath = String.format("report/" + "%s-%s-report-%s",
				timestamp, platformName.toUpperCase(), benchmarkType.toUpperCase());

		if(Files.exists(Paths.get(outputDirectoryPath))) {
			throw new IllegalStateException(
					String.format("Benchmark aborted: existing benchmark report detected at %s.", outputDirectoryPath));
		}
		return outputDirectoryPath;
	}


	protected boolean verifyGraphInfo(StandardGraph graph, Graph graphSet) {

		boolean eqNumVertices = graphSet.getSourceGraph().getNumberOfVertices() == graph.vertexSize;
		boolean eqNumEdges = graphSet.getSourceGraph().getNumberOfEdges() == graph.edgeSize;
		boolean eqDirectedness = graphSet.getSourceGraph().isDirected() == graph.isDirected;
		boolean eqProperties = graphSet.getSourceGraph().hasEdgeProperties() == graph.hasProperty;

		boolean isValid = eqNumVertices && eqNumEdges && eqDirectedness && eqProperties;

		if (!isValid) {
			LOG.info(String.format("Graph %s does not match expectation.", graph.fileName));
			LOG.info(String.format("Num vertices acutal : %s, expected %s.",
					graphSet.getSourceGraph().getNumberOfVertices(), graph.vertexSize));
			LOG.info(String.format("Num vertices acutal : %s, expected %s.",
					graphSet.getSourceGraph().getNumberOfEdges(), graph.edgeSize));
			LOG.info(String.format("Num vertices acutal : %s, expected %s.",
					graphSet.getSourceGraph().isDirected(), graph.isDirected));
			LOG.info(String.format("Num vertices acutal : %s, expected %s.",
					graphSet.getSourceGraph().hasEdgeProperties(), graph.hasProperty));

		}
		return isValid;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(String.format("Executing a %s Benchmark on %s [%s experiments, %s jobs, %s runs]:\n",
				type.toUpperCase(), platformName.toUpperCase(), experiments.size(), jobs.size(), benchmarkRuns.size()));
		for (BenchmarkExp experiment : experiments) {
			int jobSize = experiment.getJobs().size();
			String jobTexts = "";
			for (int i = 0; i < jobSize; i++) {
				BenchmarkJob job = experiment.getJobs().get(i);
				jobTexts += String.format("%s(%sx)", job.getGraph().getName(), job.getBenchmarkRuns().size());
				if(i < jobSize -1 ) {
					jobTexts += ", ";
				}

			}
			stringBuilder.append(String.format(" Experiment %s contains %s jobs: [%s]\n", experiment.getType(), jobSize, jobTexts));
		}
		return stringBuilder.toString();
	}
}
