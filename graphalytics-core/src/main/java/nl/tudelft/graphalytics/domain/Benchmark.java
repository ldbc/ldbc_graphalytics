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
package nl.tudelft.graphalytics.domain;

import java.io.Serializable;
import java.util.UUID;

/**
 * A single benchmark in the Graphalytics benchmark suite. Consists of a single algorithm, a single graph,
 * and parameters for the algorithm.
 *
 * @author Tim Hegeman
 */
public final class Benchmark implements Serializable {

	private final String uuid;
	private final Algorithm algorithm;
	private final Graph graph;
	private final Object algorithmParameters;
	private String logPath;


	/**
	 * @param algorithm           the algorithm to run for this benchmark
	 * @param graph               the graph to run the algorithm on
	 * @param algorithmParameters parameters for the algorithm
	 */
	public Benchmark(Algorithm algorithm, Graph graph, Object algorithmParameters) {
		this.uuid = String.valueOf(UUID.randomUUID().getLeastSignificantBits() * -1l);
		this.algorithm = algorithm;
		this.graph = graph;
		this.algorithmParameters = algorithmParameters;
	}

	/**
	 * @return the algorithm to run for this benchmark
	 */
	public Algorithm getAlgorithm() {
		return algorithm;
	}

	/**
	 * @return the graph to run this benchmark on
	 */
	public Graph getGraph() {
		return graph;
	}

	/**
	 * @return parameters for the algorithm
	 */
	public Object getAlgorithmParameters() {
		return algorithmParameters;
	}

	public String getUuid() {
		return uuid;
	}

	public String getLogPath() {
		return logPath;
	}

	public void setLogPath(String outputDirectoryPath) {
		logPath = outputDirectoryPath + "/data/log/" + getUuid();
	}
}
