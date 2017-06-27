#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * Copyright 2015 - 2017 Atlarge Research Team,
 * operating at Technische Universiteit Delft
 * and Vrije Universiteit Amsterdam, the Netherlands.
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
package ${package}.graphalytics.${platform-acronym};

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import ${package}.graphalytics.BenchmarkMetrics;
import ${package}.graphalytics.domain.*;
import ${package}.graphalytics.Platform;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ${package}.graphalytics.PlatformExecutionException;
import ${package}.graphalytics.domain.algorithms.BreadthFirstSearchParameters;
import ${package}.graphalytics.domain.algorithms.CommunityDetectionLPParameters;
import ${package}.graphalytics.domain.algorithms.PageRankParameters;
import ${package}.graphalytics.domain.algorithms.SingleSourceShortestPathsParameters;
import ${package}.graphalytics.${platform-acronym}.algorithms.bfs.BreadthFirstSearchJob;
import ${package}.graphalytics.${platform-acronym}.algorithms.cdlp.CommunityDetectionJob;
import ${package}.graphalytics.${platform-acronym}.algorithms.wcc.ConnectedComponentsJob;
import ${package}.graphalytics.${platform-acronym}.algorithms.pr.PageRankJob;
import ${package}.graphalytics.${platform-acronym}.algorithms.sssp.SingleSourceShortestPathsJob;
import ${package}.graphalytics.${platform-acronym}.algorithms.lcc.LocalClusteringCoefficientJob;
import org.json.simple.JSONObject;

/**
 * ${platform-name} implementation of the Graphalytics benchmark.
 *
 * @author ${developer-name}
 */
public class ${platform-name}Platform implements Platform {
	protected static final Logger LOG = LogManager.getLogger();

	/**
	 * File name for the file storing configuration options
	 */
	public static final String PLATFORM_PROPERTIES_FILE = "${platform-acronym}.properties";

	public static String BINARY_NAME = "bin/standard/main";

	private boolean graphDirected;
	private String edgeFilePath;
	private String vertexFilePath;
	private Configuration config;

	public ${platform-name}Platform() {
		try {
			config = new PropertiesConfiguration(PLATFORM_PROPERTIES_FILE);
		} catch(ConfigurationException e) {
			LOG.warn("failed to load " + PLATFORM_PROPERTIES_FILE, e);
			config = new PropertiesConfiguration();
		}
		BINARY_NAME = "./bin/granula/main";
	}

	@Override
	public void uploadGraph(Graph graph) throws Exception {
		graphDirected = graph.getGraphFormat().isDirected();
		edgeFilePath = graph.getEdgeFilePath();
		vertexFilePath = graph.getVertexFilePath();
	}

	private void setupGraphPath(Graph graph) {
		graphDirected = graph.getGraphFormat().isDirected();
		edgeFilePath = graph.getEdgeFilePath();
		vertexFilePath = graph.getVertexFilePath();
	}

	@Override
	public PlatformBenchmarkResult executeAlgorithmOnGraph(Benchmark benchmark) throws PlatformExecutionException {
		${platform-name}Job job;
		Object params = benchmark.getAlgorithmParameters();

		setupGraphPath(benchmark.getGraph());

		switch(benchmark.getAlgorithm()) {
			case BFS:
				job = new BreadthFirstSearchJob(config, vertexFilePath, edgeFilePath,
						graphDirected, (BreadthFirstSearchParameters) params, benchmark.getId());
				break;
			case WCC:
				job = new ConnectedComponentsJob(config, vertexFilePath, edgeFilePath,
						graphDirected, benchmark.getId());
				break;
			case LCC:
				job = new LocalClusteringCoefficientJob(config, vertexFilePath, edgeFilePath,
						graphDirected, benchmark.getId());
				break;
			case CDLP:
				job = new CommunityDetectionJob(config, vertexFilePath, edgeFilePath,
						graphDirected, (CommunityDetectionLPParameters) params, benchmark.getId());
				break;
			case PR:
				job = new PageRankJob(config, vertexFilePath, edgeFilePath,
						graphDirected, (PageRankParameters) params, benchmark.getId());
				break;
			case SSSP:
				job = new SingleSourceShortestPathsJob(config, vertexFilePath, edgeFilePath,
						graphDirected, (SingleSourceShortestPathsParameters) params, benchmark.getId());
				break;
			default:
				throw new PlatformExecutionException("Unsupported algorithm");
		}

		if (benchmark.isOutputRequired()) {
			job.setOutputFile(new File(benchmark.getOutputPath()));
		}

		try {
			job.run();
		} catch (IOException|InterruptedException e) {
			throw new PlatformExecutionException("failed to execute command", e);
		}

		return new PlatformBenchmarkResult(NestedConfiguration.empty());
	}

	@Override
	public void deleteGraph(String graphName) {
		//
	}

	@Override
	public BenchmarkMetrics retrieveMetrics() {
		return new BenchmarkMetrics();
	}


	@Override
	public String getName() {
		return "${platform-acronym}";
	}

	@Override
	public NestedConfiguration getPlatformConfiguration() {
		return NestedConfiguration.empty();
	}

	public void preBenchmark(Benchmark benchmark, Path logDirectory) {
	}

	public void postBenchmark(Benchmark benchmark, Path logDirectory) {
	}

	public void enrichMetrics(BenchmarkResult benchmarkResult, Path arcDirectory) {
	}
}
