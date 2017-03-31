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
import org.apache.commons.configuration.Configuration;

/**
 * Parameters for the execution of the breadth first search algorithm.
 *
 * @author Tim Hegeman
 */
public final class BreadthFirstSearchParameters extends AlgorithmParameters {
	private final long sourceVertex;

	/**
	 * @param sourceVertex the source of the breadth first search
	 */
	public BreadthFirstSearchParameters(long sourceVertex) {
		this.sourceVertex = sourceVertex;
	}

	/**
	 * @return the source of the breadth first search
	 */
	public long getSourceVertex() {
		return sourceVertex;
	}

	/**
	 * Factory for parsing a BreadthFirstSearchParameters object from the properties of a Configuration object.
	 */
	public static final class BreadthFirstSearchParametersFactory implements
			ParameterFactory<BreadthFirstSearchParameters> {
		@Override
		public BreadthFirstSearchParameters fromConfiguration(Configuration configuration)
				throws InvalidConfigurationException {
			return new BreadthFirstSearchParameters(ConfigurationUtil.getLong(configuration, "source-vertex"));
		}
	}

	@Override
	public String toString() {
		return String.format("BFS[%s]", getDescription());
	}

	public String getDescription() {
		return String.format("src_vert=%s", sourceVertex);
	}

}
