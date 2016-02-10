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
package nl.tudelft.graphalytics.configuration;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.Graph;
import nl.tudelft.graphalytics.domain.GraphSet;
import nl.tudelft.graphalytics.domain.algorithms.AlgorithmParameters;
import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for parsing information about a single graph dataset from the benchmark configuration.
 *
 * @author Tim Hegeman
 */
public final class GraphSetParser {

	private static final Logger LOG = LogManager.getLogger();

	private final Configuration config;
	private final String name;
	private final String graphRootDirectory;
	private final String graphCacheDirectory;

	private GraphSet graphSet = null;
	private Map<Algorithm, AlgorithmParameters> algorithmParameters = null;

	public GraphSetParser(Configuration graphConfigurationSubset, String name, String graphRootDirectory,
			String graphCacheDirectory) {
		this.config = graphConfigurationSubset;
		this.name = name;
		this.graphRootDirectory = graphRootDirectory;
		this.graphCacheDirectory = graphCacheDirectory;
	}

	public GraphSet parseGraphSet() throws InvalidConfigurationException {
		if (graphSet != null) {
			return graphSet;
		}

		parse();
		return graphSet;
	}

	public Map<Algorithm, AlgorithmParameters> parseAlgorithmParameters() throws InvalidConfigurationException {
		if (algorithmParameters != null) {
			return algorithmParameters;
		}

		parse();
		return algorithmParameters;
	}

	private void parse() throws InvalidConfigurationException {
		Graph sourceGraph = parseSourceGraph();
		GraphSet.Builder builder = new GraphSet.Builder(name, sourceGraph, graphCacheDirectory);
		algorithmParameters = parseAlgorithmConfiguration();

		for (Algorithm algorithm : algorithmParameters.keySet()) {
			builder.withAlgorithm(algorithm, algorithmParameters.get(algorithm));
		}

		graphSet = builder.toGraphSet();
	}

	private Graph parseSourceGraph() throws InvalidConfigurationException {
		return new GraphParser(config, name, graphRootDirectory).parseGraph();
	}

	private Map<Algorithm, AlgorithmParameters> parseAlgorithmConfiguration() throws InvalidConfigurationException {
		Map<Algorithm, AlgorithmParameters> algorithmParameters = new HashMap<>();

		// Get list of supported algorithms
		String[] algorithmNames = ConfigurationUtil.getStringArray(config, "algorithms");
		for (String algorithmName : algorithmNames) {
			Algorithm algorithm = Algorithm.fromAcronym(algorithmName);
			if (algorithm != null) {
				AlgorithmParameters parameters = algorithm.getParameterFactory().fromConfiguration(
						config.subset(algorithm.getAcronym().toLowerCase()));
				algorithmParameters.put(algorithm, parameters);
			} else {
				LOG.warn("Found unknown algorithm name \"" + algorithmName + "\" for graph \"" +
						name + "\".");
			}
		}

		return algorithmParameters;
	}

}