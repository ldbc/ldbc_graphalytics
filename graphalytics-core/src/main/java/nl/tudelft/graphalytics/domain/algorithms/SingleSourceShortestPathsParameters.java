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
package nl.tudelft.graphalytics.domain.algorithms;

import nl.tudelft.graphalytics.configuration.ConfigurationUtil;
import nl.tudelft.graphalytics.configuration.InvalidConfigurationException;
import nl.tudelft.graphalytics.domain.graph.Property;
import nl.tudelft.graphalytics.domain.graph.PropertyList;
import nl.tudelft.graphalytics.domain.graph.PropertyType;
import org.apache.commons.configuration.Configuration;

/**
 * Parameters for the execution of the single source shortest paths algorithm.
 *
 * @author Tim Hegeman
 */
public final class SingleSourceShortestPathsParameters extends AlgorithmParameters {

	private static final String WEIGHT_PROPERTY_PROPERTY = "weight-property";
	private static final String SOURCE_VERTEX_PROPERTY = "source-vertex";

	private final String weightPropertyName;
	private final long sourceVertex;

	/**
	 * @param weightPropertyName name of the edge property that represents edge weights
	 * @param sourceVertex       the source to start the shortest paths algorithm from
	 */
	public SingleSourceShortestPathsParameters(String weightPropertyName, long sourceVertex) {
		this.weightPropertyName = weightPropertyName;
		this.sourceVertex = sourceVertex;
	}

	/**
	 * @return name of the edge property that represents edge weights
	 */
	public String getWeightPropertyName() {
		return weightPropertyName;
	}

	/**
	 * @return the source to start the shortest paths algorithm from
	 */
	public long getSourceVertex() {
		return sourceVertex;
	}

	@Override
	public PropertyList getRequiredEdgeProperties() {
		return new PropertyList(new Property(weightPropertyName, PropertyType.REAL));
	}

	public static final class SingleSourceShortestPathsParametersFactory implements
			ParameterFactory<SingleSourceShortestPathsParameters> {

		@Override
		public SingleSourceShortestPathsParameters fromConfiguration(Configuration configuration)
				throws InvalidConfigurationException {
			return new SingleSourceShortestPathsParameters(
					ConfigurationUtil.getString(configuration, WEIGHT_PROPERTY_PROPERTY),
					ConfigurationUtil.getLong(configuration, SOURCE_VERTEX_PROPERTY));
		}

	}

}
