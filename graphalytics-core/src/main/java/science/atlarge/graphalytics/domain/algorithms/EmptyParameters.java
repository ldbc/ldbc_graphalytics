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

import org.apache.commons.configuration.Configuration;

/**
 * Default parameters class for algorithms without parameters.
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public final class EmptyParameters extends AlgorithmParameters {

	public static final class EmptyParametersFactory implements ParameterFactory<EmptyParameters> {

		@Override
		public EmptyParameters fromConfiguration(Configuration configuration) {
			return new EmptyParameters();
		}

	}

	@Override
	public String toString() {
		return String.format("[%s]", getDescription());
	}

	public String getDescription() {
		return String.format("none");
	}
}
