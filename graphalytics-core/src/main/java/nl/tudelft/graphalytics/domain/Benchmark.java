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
import java.util.Random;
import java.util.UUID;

/**
 * A single benchmark in the Graphalytics benchmark suite. Consists of a single algorithm, a single graph,
 * and parameters for the algorithm.
 *
 * @author Tim Hegeman
 */
public final class Benchmark implements Serializable {

	private final String name;
	private final String id;
	private final Algorithm algorithm;
	private final Graph graph;
	private final Object algorithmParameters;
	private final boolean outputRequired;
	private final String outputPath;
	private final boolean validationRequired;
	private final String validationPath;

	/**
	 * @param algorithm           the algorithm to run for this benchmark
	 * @param graph               the graph to run the algorithm on
	 * @param algorithmParameters parameters for the algorithm
	 * @param outputRequired      true iff the output of the algorithm should be written to (a) file(s)
	 * @param outputPath          the path to write the output to, or the prefix if multiple output files are required
	 */
	public Benchmark(Algorithm algorithm, Graph graph, Object algorithmParameters, boolean outputRequired,
			String outputPath, boolean validationRequired, String validationPath) {
		this.algorithm = algorithm;
		this.graph = graph;
		this.algorithmParameters = algorithmParameters;
		this.outputRequired = outputRequired;
		this.outputPath = outputPath;
		this.validationRequired = validationRequired;
		this.validationPath = validationPath;

		this.name = algorithm.getAcronym() + "-" + graph.getName();
		String random10Digit = String.valueOf(UUID.randomUUID().getLeastSignificantBits() * -1l).substring(0,10);
		this.id = name + "_b" + random10Digit;
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

	/**
	 * @return true iff the output of the algorithm should be written to (a) file(s)
	 */
	public boolean isOutputRequired() {
		return outputRequired;
	}

	/**
	 * @return the path to write the output to, or the prefix if multiple output files are required
	 */
	public String getOutputPath() {
		return outputPath;
	}

	/**
	 * @return a string uniquely identifying this benchmark to use for e.g. naming files
	 */
	public String getBenchmarkIdentificationString() {
		return graph.getName() + "-" + algorithm.getAcronym();
	}

	/**
	 * @return true iff the output of the algorithm will be validation by the benchmark suite.
	 */
	public boolean isValidationRequired() {
		return validationRequired;
	}

	/**
	 * @return the path to file containing the validation output of this benchmark.
	 */
	public String getValidationPath() {
		return validationPath;
	}


	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}
}
