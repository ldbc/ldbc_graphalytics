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
 * Parameters for the execution of the community detection algorithm, based on label propagation.
 *
 * @author Tim Hegeman
 */
public final class CommunityDetectionParameters implements Serializable {
	private final int maxIterations;

	/**
	 * @param maxIterations  the maximum number of iterations of label propagation to execute
	 */
	public CommunityDetectionParameters(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	/**
	 * @return the maximum number of iterations of label propagation to execute
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	@Override
	public String toString() {
		return "CommunityDetectionParameters(" + maxIterations + ")";
	}

	/**
	 * Factory for parsing a CommunityDetectionParameters object from the properties of a Configuration object.
	 */
	public static final class CommunityDetectionParametersFactory implements
			ParameterFactory<CommunityDetectionParameters> {
		@Override
		public CommunityDetectionParameters fromConfiguration(Configuration configuration, String baseProperty)
				throws InvalidConfigurationException {
			return new CommunityDetectionParameters(
					ConfigurationUtil.getInteger(configuration, baseProperty + ".max-iterations"));
		}
	}
}
