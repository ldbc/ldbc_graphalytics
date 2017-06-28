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
package science.atlarge.graphalytics.validation.algorithms.lcc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Container for the output of the local clustering coefficient algorithm, used by the corresponding Graphalytics
 * validation test.
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class LocalClusteringCoefficientOutput {

	private final Map<Long, Double> localClusteringCoefficients;

	/**
	 * @param localClusteringCoefficients    a map containing the local clustering coefficient of each vertex
	 */
	public LocalClusteringCoefficientOutput(Map<Long, Double> localClusteringCoefficients) {
		this.localClusteringCoefficients = new HashMap<>(localClusteringCoefficients);
	}

	/**
	 * @return a set of vertex ids for which the component id is known
	 */
	public Set<Long> getVertices() {
		return localClusteringCoefficients.keySet();
	}

	/**
	 * @param vertexId the id of a vertex in the output data
	 * @return the corresponding local clustering coefficient
	 */
	public double getLocalClusteringCoefficientForVertex(long vertexId) {
		return localClusteringCoefficients.get(vertexId);
	}

}
