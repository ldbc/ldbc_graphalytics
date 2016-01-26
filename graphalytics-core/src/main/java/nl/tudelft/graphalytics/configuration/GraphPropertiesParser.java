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

import nl.tudelft.graphalytics.domain.Graph;
import nl.tudelft.graphalytics.domain.PropertyType;
import org.apache.commons.configuration.Configuration;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for parsing information about a single graph from the benchmark configuration.
 *
 * @author Tim Hegeman
 */
public class GraphPropertiesParser {

	private final Configuration config;
	private final String name;
	private final String graphRootDirectory;

	private Graph.Builder graphBuilder;
	private Graph graph;

	public GraphPropertiesParser(Configuration graphConfigurationSubset, String name, String graphRootDirectory) {
		this.config = graphConfigurationSubset;
		this.name = name;
		this.graphRootDirectory = graphRootDirectory;
	}

	public Graph parseGraph() throws InvalidConfigurationException {
		if (graph != null) {
			return graph;
		}

		parseBasicGraphConfiguration();
		parseVertexPropertiesConfiguration();
		parseEdgePropertiesConfiguration();

		graph = graphBuilder.toGraph();
		return graph;
	}

	private void parseBasicGraphConfiguration() throws InvalidConfigurationException {
		// Read general graph information
		boolean isDirected = ConfigurationUtil.getBoolean(config, "directed");
		long vertexCount = ConfigurationUtil.getLong(config, "meta.vertices");
		long edgeCount = ConfigurationUtil.getLong(config, "meta.edges");

		// Read location information for the graph without properties
		String vertexFileName = resolveGraphPath(ConfigurationUtil.getString(config, "vertex-file"));
		String edgeFileName = resolveGraphPath(ConfigurationUtil.getString(config, "edge-file"));

		// Construct a partial Graph object
		graphBuilder = new Graph.Builder(name, vertexCount, edgeCount, isDirected, vertexFileName, edgeFileName);
	}

	private void parseVertexPropertiesConfiguration() throws InvalidConfigurationException {
		Configuration vpConfig = config.subset("vertex-properties");
		if (vpConfig.isEmpty()) {
			return;
		}

		// Read the property file path
		String vertexPropertiesFileName = resolveGraphPath(ConfigurationUtil.getString(vpConfig, "path"));

		// Read property names (requires more than zero, and non-empty entries)
		String[] propertyNames = ConfigurationUtil.getStringArray(vpConfig, "names");
		if (propertyNames.length == 0) {
			throw new InvalidConfigurationException("A graph with vertex properties must have at least one property name (\"" +
					name + "\")");
		}
		for (String propertyName : propertyNames) {
			if (propertyName.isEmpty()) {
				throw new InvalidConfigurationException("A graph with vertex properties must have non-empty property names (\"" +
						name + "\")");
			}
		}

		// Read property types (requires same number as property names, and PropertyType entries)
		List<PropertyType> propertyTypes = new ArrayList<>(propertyNames.length);
		String[] propertyTypesAsStrings = ConfigurationUtil.getStringArray(vpConfig, "types");
		if (propertyTypesAsStrings.length != propertyNames.length) {
			throw new InvalidConfigurationException("A graph with vertex properties must have an equal number of " +
					"property names and property types (\"" + name + "\")");
		}
		for (String propertyTypeAsString : propertyTypesAsStrings) {
			PropertyType propertyType = PropertyType.fromString(propertyTypeAsString);
			if (propertyType == null) {
				throw new InvalidConfigurationException("A graph with vertex properties must have valid property types (\"" +
						name + "\")");
			}
			propertyTypes.add(propertyType);
		}

		graphBuilder.withVertexProperties(vertexPropertiesFileName, Arrays.asList(propertyNames), propertyTypes);
	}

	private void parseEdgePropertiesConfiguration() throws InvalidConfigurationException {
		Configuration epConfig = config.subset("edge-properties");
		if (epConfig.isEmpty()) {
			return;
		}

		// Read the property file path
		String edgePropertiesFileName = resolveGraphPath(ConfigurationUtil.getString(epConfig, "path"));

		// Read property names (requires more than zero, and non-empty entries)
		String[] propertyNames = ConfigurationUtil.getStringArray(epConfig, "names");
		if (propertyNames.length == 0) {
			throw new InvalidConfigurationException("A graph with edge properties must have at least one property name (\"" +
					name + "\")");
		}
		for (String propertyName : propertyNames) {
			if (propertyName.isEmpty()) {
				throw new InvalidConfigurationException("A graph with edge properties must have non-empty property names (\"" +
						name + "\")");
			}
		}

		// Read property types (requires same number as property names, and PropertyType entries)
		List<PropertyType> propertyTypes = new ArrayList<>(propertyNames.length);
		String[] propertyTypesAsStrings = ConfigurationUtil.getStringArray(epConfig, "types");
		if (propertyTypesAsStrings.length != propertyNames.length) {
			throw new InvalidConfigurationException("A graph with edge properties must have an equal number of " +
					"property names and property types (\"" + name + "\")");
		}
		for (String propertyTypeAsString : propertyTypesAsStrings) {
			PropertyType propertyType = PropertyType.fromString(propertyTypeAsString);
			if (propertyType == null) {
				throw new InvalidConfigurationException("A graph with edge properties must have valid property types (\"" +
						name + "\")");
			}
			propertyTypes.add(propertyType);
		}

		graphBuilder.withEdgeProperties(edgePropertiesFileName, Arrays.asList(propertyNames), propertyTypes);
	}

	private String resolveGraphPath(String relativePath) {
		return Paths.get(graphRootDirectory, relativePath).toString();
	}

}