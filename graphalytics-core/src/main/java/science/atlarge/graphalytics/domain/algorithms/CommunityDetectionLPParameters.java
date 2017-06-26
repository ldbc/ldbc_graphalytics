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

import science.atlarge.graphalytics.configuration.ConfigurationUtil;
import science.atlarge.graphalytics.configuration.InvalidConfigurationException;
import org.apache.commons.configuration.Configuration;

/**
 * Parameters for the execution of the community detection algorithm, based on label propagation.
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public final class CommunityDetectionLPParameters extends AlgorithmParameters {
	private final int maxIterations;

	/**
	 * @param maxIterations the maximum number of iterations of label propagation to execute
	 */
	public CommunityDetectionLPParameters(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	/**
	 * @return the maximum number of iterations of label propagation to execute
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	/**
	 * Factory for parsing a CommunityDetectionLPParameters object from the properties of a Configuration object.
	 */
	public static final class CommunityDetectionLPParametersFactory implements
			ParameterFactory<CommunityDetectionLPParameters> {
		@Override
		public CommunityDetectionLPParameters fromConfiguration(Configuration configuration)
				throws InvalidConfigurationException {
			return new CommunityDetectionLPParameters(ConfigurationUtil.getInteger(configuration, "max-iterations"));
		}
	}

	@Override
	public String toString() {
		return String.format("CDLP[%s]", getDescription());
	}

	public String getDescription() {
		return String.format("iter=%s",	maxIterations);
	}
}
