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

import java.io.Serializable;

/**
 * Parameters for the execution of the forest fire model algorithm.
 *
 * @author Tim Hegeman
 */
public final class ForestFireModelParameters implements Serializable {
	private final long maxId;
	private final float pRatio;
	private final float rRatio;
	private final int maxIterations;
	private final int numNewVertices;

	/**
	 * @param maxId          the highest used vertex ID in the graph
	 * @param pRatio         geometric distribution parameter for the forward burning probability
	 * @param rRatio         geometric distribution parameter for the backward burning probability
	 * @param maxIterations  maximum number of iterations of the forest fire model to execute
	 * @param numNewVertices the number of new vertices to add to the graph
	 */
	public ForestFireModelParameters(long maxId, float pRatio, float rRatio, int maxIterations, int numNewVertices) {
		this.maxId = maxId;
		this.pRatio = pRatio;
		this.rRatio = rRatio;
		this.maxIterations = maxIterations;
		this.numNewVertices = numNewVertices;
	}

	/**
	 * @return the highest used vertex ID in the graph
	 */
	public long getMaxId() {
		return maxId;
	}

	/**
	 * @return geometric distribution parameter for the forward burning probability
	 */
	public float getPRatio() {
		return pRatio;
	}

	/**
	 * @return geometric distribution parameter for the backward burning probability
	 */
	public float getRRatio() {
		return rRatio;
	}

	/**
	 * @return maximum number of iterations of the forest fire model to execute
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	/**
	 * @return the number of new vertices to add to the graph
	 */
	public int getNumNewVertices() {
		return numNewVertices;
	}

	/**
	 * Factory for parsing a ForestFireModelParameters object from the properties of a Configuration object.
	 */
	public static final class ForestFireModelParametersFactory implements ParameterFactory<ForestFireModelParameters> {
		@Override
		public ForestFireModelParameters fromConfiguration(Configuration configuration, String baseProperty)
				throws InvalidConfigurationException {
			return new ForestFireModelParameters(ConfigurationUtil.getLong(configuration, baseProperty + ".max-id"),
					ConfigurationUtil.getFloat(configuration, baseProperty + ".pratio"),
					ConfigurationUtil.getFloat(configuration, baseProperty + ".rratio"),
					ConfigurationUtil.getInteger(configuration, baseProperty + ".max-iterations"),
					ConfigurationUtil.getInteger(configuration, baseProperty + ".new-vertices"));
		}
	}

}
