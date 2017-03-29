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
package nl.tudelft.graphalytics.domain.benchmark;

import nl.tudelft.graphalytics.domain.algorithms.Algorithm;
import nl.tudelft.graphalytics.domain.algorithms.AlgorithmParameters;
import nl.tudelft.graphalytics.domain.graph.Graph;
import nl.tudelft.graphalytics.domain.graph.GraphSet;
import nl.tudelft.graphalytics.util.UuidUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A single benchmark in the Graphalytics benchmark suite. Consists of a single algorithm, a single graph,
 * and parameters for the algorithm.
 *
 * @author Tim Hegeman
 */
public final class BenchmarkRun implements Serializable {

	private String id;
	private Algorithm algorithm;
	private GraphSet graphSet;
	private Graph graph;
	private AlgorithmParameters algorithmParameters;

	private boolean outputRequired;
	private boolean validationRequired;

	private Path outputDir;
	private Path validationDir;
	private Path logDir;

	/**
	 * @param algorithm           the algorithm to run for this benchmark
	 * @param graph               the graph to run the algorithm on
	 * @param algorithmParameters parameters for the algorithm
	 * @param outputRequired      true iff the output of the algorithm should be written to (a) file(s)
	 * @param outputDir          the path to write the output to, or the prefix if multiple output files are required
	 */
	public BenchmarkRun(Algorithm algorithm, GraphSet graphSet, Graph graph, AlgorithmParameters algorithmParameters, boolean outputRequired,
						Path outputDir, boolean validationRequired, Path validationDir, Path logDir) {

		this.id = UuidUtil.getRandomUUID("r", 6);
		this.algorithm = algorithm;
		this.graphSet = graphSet;
		this.graph = graph;
		this.algorithmParameters = algorithmParameters;

		this.outputRequired = outputRequired;
		this.validationRequired = validationRequired;

		this.outputDir = outputDir.resolve(getName());
		this.validationDir = validationDir.resolve(graphSet.getName() + "-" + algorithm.getAcronym());
		this.logDir = logDir.resolve("log").resolve(getName());

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
	public Path getOutputDir() {
		return outputDir;
	}

	/**
	 * @return a string uniquely identifying this benchmark to use for e.g. naming files
	 */
	public String getName() {
		return String.format("%s-%s-%s", id, algorithm.getAcronym(), graph.getName());
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
	public Path getValidationDir() {
		return validationDir;
	}


	public String getId() {
		return id;
	}

	public Path getLogDir() {
		return logDir;
	}

	public GraphSet getGraphSet() {
		return graphSet;
	}

	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.writeObject(id);
		stream.writeObject(algorithm);
		stream.writeObject(graphSet);
		stream.writeObject(graph);
		stream.writeObject(algorithmParameters);

		stream.writeBoolean(outputRequired);
		stream.writeBoolean(validationRequired);

		stream.writeObject(outputDir.toAbsolutePath().toString()	);
		stream.writeObject(validationDir.toAbsolutePath().toString());
		stream.writeObject(logDir.toAbsolutePath().toString());
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		id = (String) stream.readObject();
		algorithm = (Algorithm) stream.readObject();
		graphSet = (GraphSet) stream.readObject();
		graph = (Graph) stream.readObject();
		algorithmParameters = (AlgorithmParameters) stream.readObject();

		outputRequired = stream.readBoolean();
		validationRequired =  stream.readBoolean();

		outputDir = Paths.get(((String) stream.readObject()));
		validationDir = Paths.get(((String) stream.readObject()));
		logDir = Paths.get(((String) stream.readObject()));
	}

	@Override
	public String toString() {
		return "BenchmarkRun{" +
				"id='" + id + '\'' +
				", algorithm=" + algorithm.getAcronym() +
				", graphSet=" + graphSet.getName() +
				", graph=" + graph.getName() +
				", algorithmParameters=" + algorithmParameters +
				", outputRequired=" + outputRequired +
				", validationRequired=" + validationRequired +
				", outputDir=" + outputDir +
				", validationDir=" + validationDir +
				", logDir=" + logDir +
				'}';
	}

}
