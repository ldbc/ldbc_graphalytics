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
package science.atlarge.graphalytics.domain.algorithms;

import science.atlarge.graphalytics.domain.graph.PropertyList;

import java.io.Serializable;

/**
 * Base class for algorithm parameters. Defines several functions to specify which properties in a property graph are
 * required for the algorithm to run, given the algorithm's parameters.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public abstract class AlgorithmParameters implements Serializable {

	/**
	 * Returns a list of (names and types of) vertex properties required for the algorithm, based on the algorithm
	 * parameters. The algorithm-specific configuration should allow the end-user to introduce a mapping from vertex
	 * property names in a graph to algorithm-specific concepts (e.g., weights).
	 *
	 * @return an ordered list of vertex property names required for the algorithm
	 */
	public PropertyList getRequiredVertexProperties() {
		return new PropertyList();
	}

	/**
	 * Returns a list of (names and types of) edge properties required for the algorithm, based on the algorithm
	 * parameters. The algorithm-specific configuration should allow the end-user to introduce a mapping from edge
	 * property names in a graph to algorithm-specific concepts (e.g., weights).
	 *
	 * @return an ordered list of edge property names required for the algorithm
	 */
	public PropertyList getRequiredEdgeProperties() {
		return new PropertyList();
	}

	public abstract String getDescription();
}
