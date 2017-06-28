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
package science.atlarge.graphalytics.configuration;

import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import science.atlarge.graphalytics.domain.graph.Property;
import science.atlarge.graphalytics.domain.graph.PropertyList;
import science.atlarge.graphalytics.domain.graph.PropertyType;
import org.apache.commons.configuration.Configuration;

import java.nio.file.Paths;

/**
 * Utility class for parsing information about a single graph from the benchmark configuration.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public final class FormattedGraphParser {

	private final Configuration config;
	private final String name;
	private final String graphRootDirectory;

	private long vertexCount;
	private long edgeCount;
	private boolean isDirected;
	private String vertexFilePath;
	private String edgeFilePath;
	private PropertyList vertexProperties;
	private PropertyList edgeProperties;

	private FormattedGraph formattedGraph;

	public FormattedGraphParser(Configuration graphConfigurationSubset, String name, String graphRootDirectory) {
		this.config = graphConfigurationSubset;
		this.name = name;
		this.graphRootDirectory = graphRootDirectory;
	}

	public FormattedGraph parseFormattedGraph() throws InvalidConfigurationException {
		if (formattedGraph != null) {
			return formattedGraph;
		}

		parseBasicGraphConfiguration();
		parseVertexPropertiesConfiguration();
		parseEdgePropertiesConfiguration();

		formattedGraph = new FormattedGraph(name, vertexCount, edgeCount, isDirected, vertexFilePath, edgeFilePath, vertexProperties, edgeProperties);
		return formattedGraph;
	}

	private void parseBasicGraphConfiguration() throws InvalidConfigurationException {
		// Read general graph information
		isDirected = ConfigurationUtil.getBoolean(config, "directed");
		vertexCount = ConfigurationUtil.getLong(config, "meta.vertices");
		edgeCount = ConfigurationUtil.getLong(config, "meta.edges");

		// Read location information for the graph
		vertexFilePath = resolveGraphPath(ConfigurationUtil.getString(config, "vertex-file"));
		edgeFilePath = resolveGraphPath(ConfigurationUtil.getString(config, "edge-file"));
	}

	private void parseVertexPropertiesConfiguration() throws InvalidConfigurationException {
		Configuration vpConfig = config.subset("vertex-properties");
		if (vpConfig.isEmpty()) {
			vertexProperties = new PropertyList();
		} else {
			vertexProperties = parsePropertyList(vpConfig, "vertex", name);
		}
	}

	private void parseEdgePropertiesConfiguration() throws InvalidConfigurationException {
		Configuration epConfig = config.subset("edge-properties");
		if (epConfig.isEmpty()) {
			edgeProperties = new PropertyList();
		} else {
			edgeProperties = parsePropertyList(epConfig, "edge", name);
		}
	}

	private static PropertyList parsePropertyList(Configuration config, String errorMessagePropertyKind, String errorMessageGraphName)
			throws InvalidConfigurationException {
		// Retrieve the property names and types
		String[] propertyNames = ConfigurationUtil.getStringArray(config, "names");
		String[] propertyTypes = ConfigurationUtil.getStringArray(config, "types");
		// Check if there are an equal number of names and types, and if there are any at all
		if (propertyNames.length != propertyTypes.length) {
			throw new InvalidConfigurationException("A graph with " + errorMessagePropertyKind +
					" properties must have an equal number of property names and property types (\"" +
					errorMessageGraphName + "\")");
		}
		if (propertyNames.length == 0) {
			return new PropertyList();
		}

		// Iterate through the property names and types to create Property objects while performing sanity checks
		Property[] properties = new Property[propertyNames.length];
		for (int i = 0; i < properties.length; i++) {
			String propertyName = propertyNames[i];
			PropertyType propertyType = PropertyType.fromString(propertyTypes[i]);

			if (propertyName.isEmpty()) {
				throw new InvalidConfigurationException("A graph with " + errorMessagePropertyKind +
						" properties must have non-empty property names (\"" + errorMessageGraphName + "\")");
			}
			if (propertyType == null) {
				throw new InvalidConfigurationException("A graph with " + errorMessagePropertyKind +
						" properties must have valid property types (\"" + errorMessageGraphName + "\")");
			}

			properties[i] = new Property(propertyName, propertyType);
		}

		return new PropertyList(properties);
	}

	private String resolveGraphPath(String relativePath) {
		return Paths.get(graphRootDirectory, relativePath).toString();
	}

}