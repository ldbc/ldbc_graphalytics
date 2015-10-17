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
 * Parameters for the execution of the PageRank algorithm. These parameters include a set number of iterations to run
 * the PageRank algorithm for. This number is determined to be the number of iterations until for each vertex the
 * following holds: absolute value of ((new PageRank - old PageRank) / old PageRank) is at most 1e-5.
 *
 * @author Tim Hegeman
 */
public class PageRankParameters implements Serializable {

	private final double dampingFactor;
	private final int numberOfIterations;

	/**
	 * @param dampingFactor      the damping factor to use for the PageRank algorithm
	 * @param numberOfIterations the number of iterations to run the PageRank algorithm for
	 */
	public PageRankParameters(double dampingFactor, int numberOfIterations) {
		this.dampingFactor = dampingFactor;
		this.numberOfIterations = numberOfIterations;
	}

	/**
	 * @return the damping factor to use for the PageRank algorithm
	 */
	public double getDampingFactor() {
		return dampingFactor;
	}

	/**
	 * @return the number of iterations to run the PageRank algorithm for
	 */
	public int getNumberOfIterations() {
		return numberOfIterations;
	}

	@Override
	public String toString() {
		return "PageRankParameters(" + dampingFactor + ',' + numberOfIterations + ')';
	}

	/**
	 * Factory for parsing a PageRankParameters object from the properties of a Configuration object.
	 */
	public static final class PageRankParametersFactory implements ParameterFactory<PageRankParameters> {

		@Override
		public PageRankParameters fromConfiguration(Configuration configuration, String baseProperty)
				throws InvalidConfigurationException {
			return new PageRankParameters(ConfigurationUtil.getDouble(configuration, baseProperty + ".damping-factor"),
					ConfigurationUtil.getInteger(configuration, baseProperty + ".num-iterations"));
		}

	}

}
