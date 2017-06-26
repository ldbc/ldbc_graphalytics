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
package science.atlarge.graphalytics.domain.graph;

import science.atlarge.graphalytics.configuration.InvalidConfigurationException;
import science.atlarge.graphalytics.domain.algorithms.Algorithm;
import science.atlarge.graphalytics.domain.algorithms.AlgorithmParameters;

import java.io.Serializable;
import java.nio.file.Paths;
import java.util.*;

/**
 * Class representing a collection of graphs derived from a single dataset. Its primary use is to allow a dataset
 * with multiple edge and/or vertex properties to be used for algorithms requiring different subsets of these
 * properties.
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public final class Graph implements Serializable {

	private final String graphName;
	private final FormattedGraph sourceGraph;
	private final Map<Algorithm, FormattedGraph> graphPerAlgorithm;
	private final Set<FormattedGraph> formattedGraphs;
	private final Map<Algorithm, AlgorithmParameters> algorithmParameters;

	private Graph(String graphName, FormattedGraph sourceGraph,
				  Map<Algorithm, FormattedGraph> graphPerAlgorithm,
				  Map<Algorithm, AlgorithmParameters> algorithmParameters) {
		this.graphName = graphName;
		this.sourceGraph = sourceGraph;
		this.graphPerAlgorithm = Collections.unmodifiableMap(graphPerAlgorithm);
		this.algorithmParameters = Collections.unmodifiableMap(algorithmParameters);

		Set<FormattedGraph> formattedGraphs = new HashSet<>(graphPerAlgorithm.values());
		formattedGraphs.add(sourceGraph);
		this.formattedGraphs = Collections.unmodifiableSet(formattedGraphs);

		for (FormattedGraph formattedGraph : this.formattedGraphs) {
			formattedGraph.setGraph(this);
		}
	}

	public String getName() {
		return graphName;
	}

	public FormattedGraph getSourceGraph() {
		return sourceGraph;
	}

	public Map<Algorithm, FormattedGraph> getGraphPerAlgorithm() {
		return graphPerAlgorithm;
	}

	public Map<Algorithm, AlgorithmParameters> getAlgorithmParameters() {
		return algorithmParameters;
	}

	public Set<FormattedGraph> getFormattedGraphs() {
		return formattedGraphs;
	}

	public boolean isDirected() {
		return sourceGraph.isDirected();
	}

	public long getNumberOfVertices() {
		return sourceGraph.getNumberOfVertices();
	}

	public long getNumberOfEdges() {
		return sourceGraph.getNumberOfEdges();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Graph graph = (Graph)o;

		return graphName.equals(graph.graphName);

	}

	@Override
	public int hashCode() {
		return graphName.hashCode();
	}


	public Set<String> listFormattedGraphs() {
		Set<String> graphNames = new HashSet<>();
		for (FormattedGraph formattedGraph : formattedGraphs) {
			graphNames.add(formattedGraph.getName());
		}
		return graphNames;
	}

	public static class Builder {

		private final String graphName;
		private final FormattedGraph sourceGraph;
		private final String graphCacheDirectory;
		private final Map<Algorithm, FormattedGraph> graphPerAlgorithm;
		private final Map<Algorithm, AlgorithmParameters> algorithmParameters;
		private final Map<PropertyLists, FormattedGraph> graphPerProperties;

		public Builder(String graphName, FormattedGraph sourceGraph, String graphCacheDirectory) {
			this.graphName = graphName;
			this.sourceGraph = sourceGraph;
			this.graphCacheDirectory = graphCacheDirectory;
			this.graphPerAlgorithm = new HashMap<>();
			this.algorithmParameters = new HashMap<>();

			this.graphPerProperties = new HashMap<>();
			this.graphPerProperties.put(new PropertyLists(sourceGraph.getVertexProperties(),
					sourceGraph.getEdgeProperties()), sourceGraph);
		}

		public Builder withAlgorithm(Algorithm algorithm, AlgorithmParameters parameters)
				throws InvalidConfigurationException {
			// Get a list of properties required for the algorithm
			PropertyList vertexProperties = parameters.getRequiredVertexProperties();
			PropertyList edgeProperties = parameters.getRequiredEdgeProperties();

			algorithmParameters.put(algorithm, parameters);

			// Check if the required combination of vertex and edge properties already exists
			PropertyLists propertyLists = new PropertyLists(vertexProperties, edgeProperties);
			if (graphPerProperties.containsKey(propertyLists)) {
				FormattedGraph formattedGraph = graphPerProperties.get(propertyLists);
				graphPerAlgorithm.put(algorithm, formattedGraph);
				return this;
			}
			// Otherwise, build a new Graph object

			// Verify if the required properties exist in the graph
			if (!vertexProperties.isSubsetOf(sourceGraph.getVertexProperties())) {
				throw new InvalidConfigurationException("Some vertex property required by algorithm \"" +
						algorithm + "\" does not exist in graph \"" + graphName + "\".");
			}
			if (!edgeProperties.isSubsetOf(sourceGraph.getEdgeProperties())) {
				throw new InvalidConfigurationException("Some edge property required by algorithm \"" +
						algorithm + "\" does not exist in graph \"" + graphName + "\".");
			}

			// Select the filenames for the vertex and edge data
			String vertexFilename = vertexProperties.equals(sourceGraph.getVertexProperties()) ?
					sourceGraph.getVertexFilePath() :
					generateCacheFilename(vertexProperties, false);
			String edgeFilename = edgeProperties.equals(sourceGraph.getEdgeProperties()) ?
					sourceGraph.getEdgeFilePath() :
					generateCacheFilename(edgeProperties, true);

			// Create the Graph object and add it
			FormattedGraph formattedGraph = new FormattedGraph(graphName, sourceGraph.getNumberOfVertices(),
					sourceGraph.getNumberOfEdges(), sourceGraph.isDirected(), vertexFilename, edgeFilename,
					vertexProperties, edgeProperties);
			graphPerProperties.put(propertyLists, formattedGraph);
			graphPerAlgorithm.put(algorithm, formattedGraph);
			return this;
		}

		private String generateCacheFilename(PropertyList properties, boolean isEdgeFile) {
			StringBuilder filename = new StringBuilder(graphName);
			for (Property property : properties) {
				filename.append('.');
				filename.append(property.getName());
			}
			filename.append('.').append(isEdgeFile ? 'e' : 'v');
			return Paths.get(graphCacheDirectory, filename.toString()).toString();
		}

		public Graph toGraphSet() {
			return new Graph(graphName, sourceGraph, new HashMap<>(graphPerAlgorithm), new HashMap<>(algorithmParameters));
		}

		private static class PropertyLists {

			private final PropertyList vertexProperties;
			private final PropertyList edgeProperties;
			private final int cachedHashCode;

			public PropertyLists(PropertyList vertexProperties, PropertyList edgeProperties) {
				this.vertexProperties = vertexProperties;
				this.edgeProperties = edgeProperties;
				this.cachedHashCode = computeHashCode();
			}

			private int computeHashCode() {
				return 31 * vertexProperties.hashCode() + edgeProperties.hashCode();
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;

				PropertyLists that = (PropertyLists)o;

				if (cachedHashCode != that.cachedHashCode) return false;
				if (!vertexProperties.equals(that.vertexProperties)) return false;
				return edgeProperties.equals(that.edgeProperties);

			}

			@Override
			public int hashCode() {
				return cachedHashCode;
			}

		}

	}

}
