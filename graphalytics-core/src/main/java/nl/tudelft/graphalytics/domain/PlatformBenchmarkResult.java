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

import java.io.Serializable;

/**
 * Platform-dependent information regarding the results of a single benchmark run.
 *
 * @author Tim Hegeman
 */
public final class PlatformBenchmarkResult implements Serializable {

	private final NestedConfiguration platformConfiguration;
	private boolean completeSuccessfully;

	/**
	 * @param platformConfiguration platform-specific configuration used for this benchmark
	 */
	public PlatformBenchmarkResult(NestedConfiguration platformConfiguration) {
		this.platformConfiguration = platformConfiguration;
	}

	public PlatformBenchmarkResult(NestedConfiguration platformConfiguration, boolean completeSuccessfully) {
		this.platformConfiguration = platformConfiguration;
		this.completeSuccessfully = completeSuccessfully;
	}

	/**
	 * @return platform-specific configuration used for this benchmark
	 */
	public NestedConfiguration getPlatformConfiguration() {
		return platformConfiguration;
	}

	public boolean isCompleteSuccessfully() {
		return completeSuccessfully;
	}

	public void setCompleteSuccessfully(boolean completeSuccessfully) {
		this.completeSuccessfully = completeSuccessfully;
	}
}