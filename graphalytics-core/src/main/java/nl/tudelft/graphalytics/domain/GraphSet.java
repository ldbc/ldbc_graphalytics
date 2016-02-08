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

import nl.tudelft.graphalytics.configuration.InvalidConfigurationException;
import nl.tudelft.graphalytics.domain.algorithms.AlgorithmParameters;
import nl.tudelft.graphalytics.domain.graph.Property;
import nl.tudelft.graphalytics.domain.graph.PropertyList;

import java.nio.file.Paths;
import java.util.*;

/**
 * Class representing a collection of graphs derived from a single dataset. Its primary use is to allow a dataset
 * with multiple edge and/or vertex properties to be used for algorithms requiring different subsets of these
 * properties.
 *
 * @author Tim Hegeman
 */
public final class GraphSet {

	private final Graph sourceGraph;
	private final Map<Algorithm, Graph> graphPerAlgorithm;
	private final Set<Graph> derivedGraphs;

	private GraphSet(Graph sourceGraph, Map<Algorithm, Graph> graphPerAlgorithm) {
		this.sourceGraph = sourceGraph;
		this.graphPerAlgorithm = Collections.unmodifiableMap(graphPerAlgorithm);
		this.derivedGraphs = Collections.unmodifiableSet(new HashSet<>(graphPerAlgorithm.values()));
	}

	public Graph getSourceGraph() {
		return sourceGraph;
	}

	public Map<Algorithm, Graph> getGraphPerAlgorithm() {
		return graphPerAlgorithm;
	}

	public Set<Graph> getDerivedGraphs() {
		return derivedGraphs;
	}

	public static class Builder {

		private final Graph sourceGraph;
		private final String graphCacheDirectory;
		private final Map<Algorithm, Graph> graphPerAlgorithm;

		private final Map<PropertyLists, Graph> graphPerProperties;

		public Builder(Graph sourceGraph, String graphCacheDirectory) {
			this.sourceGraph = sourceGraph;
			this.graphCacheDirectory = graphCacheDirectory;
			this.graphPerAlgorithm = new HashMap<>();

			this.graphPerProperties = new HashMap<>();
			this.graphPerProperties.put(new PropertyLists(sourceGraph.getVertexProperties(),
					sourceGraph.getEdgeProperties()), sourceGraph);
		}

		public Builder withAlgorithm(Algorithm algorithm, AlgorithmParameters parameters)
				throws InvalidConfigurationException {
			// Get a list of properties required for the algorithm
			PropertyList vertexProperties = parameters.getRequiredVertexProperties();
			PropertyList edgeProperties = parameters.getRequiredEdgeProperties();

			// Check if the required combination of vertex and edge properties already exists
			PropertyLists propertyLists = new PropertyLists(vertexProperties, edgeProperties);
			if (graphPerProperties.containsKey(propertyLists)) {
				Graph graph = graphPerProperties.get(propertyLists);
				graphPerAlgorithm.put(algorithm, graph);
				return this;
			}
			// Otherwise, build a new Graph object

			// Verify if the required properties exist in the graph
			if (!vertexProperties.isSubsetOf(sourceGraph.getVertexProperties())) {
				throw new InvalidConfigurationException("Some vertex property required by algorithm \"" +
						algorithm + "\" does not exist in graph \"" + sourceGraph.getName() + "\".");
			}
			if (!edgeProperties.isSubsetOf(sourceGraph.getEdgeProperties())) {
				throw new InvalidConfigurationException("Some edge property required by algorithm \"" +
						algorithm + "\" does not exist in graph \"" + sourceGraph.getName() + "\".");
			}

			// Select the filenames for the vertex and edge data
			String vertexFilename = vertexProperties.equals(sourceGraph.getVertexProperties()) ?
					sourceGraph.getVertexFilePath() :
					generateCacheFilename(vertexProperties, false);
			String edgeFilename = edgeProperties.equals(sourceGraph.getEdgeProperties()) ?
					sourceGraph.getEdgeFilePath() :
					generateCacheFilename(edgeProperties, true);

			// Create the Graph object and add it
			Graph graph = new Graph(sourceGraph.getName(), sourceGraph.getNumberOfVertices(),
					sourceGraph.getNumberOfEdges(), sourceGraph.isDirected(), vertexFilename, edgeFilename,
					vertexProperties, edgeProperties);
			graphPerProperties.put(propertyLists, graph);
			graphPerAlgorithm.put(algorithm, graph);
			return this;
		}

		private String generateCacheFilename(PropertyList properties, boolean isEdgeFile) {
			StringBuilder filename = new StringBuilder(sourceGraph.getName());
			for (Property property : properties) {
				filename.append('.');
				filename.append(property.getName());
			}
			filename.append('.').append(isEdgeFile ? 'e' : 'v');
			return Paths.get(graphCacheDirectory, filename.toString()).toString();
		}

		public GraphSet toGraphSet() {
			return new GraphSet(sourceGraph, new HashMap<>(graphPerAlgorithm));
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
