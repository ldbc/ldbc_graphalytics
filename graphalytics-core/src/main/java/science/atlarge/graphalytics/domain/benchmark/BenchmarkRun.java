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
package science.atlarge.graphalytics.domain.benchmark;

import science.atlarge.graphalytics.domain.algorithms.Algorithm;
import science.atlarge.graphalytics.domain.algorithms.AlgorithmParameters;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import science.atlarge.graphalytics.util.UuidUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * A single benchmark in the Graphalytics benchmark suite. Consists of a single algorithm, a single graph,
 * and parameters for the algorithm.
 *
 * @author Mihai Capotă
 * @author Stijn Heldens
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public final class BenchmarkRun implements Serializable {

	private String id;
	private Algorithm algorithm;
	private Graph graph;

	private int timeout;
	private boolean outputRequired;
	private boolean validationRequired;

	private Path logDir;
	private Path outputDir;
	private Path validationDir;

	private Map<String, String> runTimeInfo;

	/**
	 * @param algorithm           the algorithm to run for this benchmark
	 * @param outputRequired      true iff the output of the algorithm should be written to (a) file(s)
	 * @param outputDir          the path to write the output to, or the prefix if multiple output files are required
	 */
	public BenchmarkRun(Algorithm algorithm, Graph graph,
						int timeout, boolean outputRequired, boolean validationRequired,
						Path logDir, Path outputDir, Path validationDir) {

		this.id = UuidUtil.getRandomUUID("r", 6);
		this.algorithm = algorithm;
		this.graph = graph;

		this.timeout = timeout;
		this.outputRequired = outputRequired;
		this.validationRequired = validationRequired;

		this.logDir = logDir.resolve(getName());
		this.outputDir = outputDir.resolve(getName());
		this.validationDir = validationDir.resolve(graph.getName() + "-" + algorithm.getAcronym());

		this.runTimeInfo = new HashMap<>();
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
	public FormattedGraph getFormattedGraph() {
		return graph.getGraphPerAlgorithm().get(algorithm);
	}

	/**
	 * @return parameters for the algorithm
	 */
	public AlgorithmParameters getAlgorithmParameters() {
		return graph.getAlgorithmParameters().get(algorithm);
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

	public Graph getGraph() {
		return graph;
	}

	public String getRuntimeInfo(String key) {
		return runTimeInfo.get(key);
	}

	public void setRuntimeInfo(String key, String value) {
		this.runTimeInfo.put(key, value);
	}

	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.writeObject(id);
		stream.writeObject(algorithm);
		stream.writeObject(graph);

		stream.writeInt(timeout);
		stream.writeBoolean(outputRequired);
		stream.writeBoolean(validationRequired);


		stream.writeObject(logDir.toAbsolutePath().toString());
		stream.writeObject(outputDir.toAbsolutePath().toString());
		stream.writeObject(validationDir.toAbsolutePath().toString());
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		id = (String) stream.readObject();
		algorithm = (Algorithm) stream.readObject();
		graph = (Graph) stream.readObject();

		timeout = stream.readInt();
		outputRequired = stream.readBoolean();
		validationRequired =  stream.readBoolean();


		logDir = Paths.get(((String) stream.readObject()));
		outputDir = Paths.get(((String) stream.readObject()));
		validationDir = Paths.get(((String) stream.readObject()));
	}

	public String getSpecification() {
		return String.format("%s, %s[%s], %s",
				id, algorithm.getAcronym(),
				getAlgorithmParameters().getDescription(),
				graph.getName());
	}

	public String getConfigurations() {
		return String.format("timeout=%ss, output=%s, validation=%s",
				timeout,
				outputRequired ? "enabled" : "disabled",
				validationRequired ? "enabled" : "disabled");
	}


	@Override
	public String toString() {
		return String.format("BenchmarkRun [%s]", getSpecification());
	}


}
