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
package science.atlarge.graphalytics.report.result;

import java.io.Serializable;

/**
 * Platform-dependent information regarding the results of a single benchmark run.
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public final class PlatformBenchmarkResult implements Serializable {

	private boolean completeSuccessfully;


	public PlatformBenchmarkResult() {
	}


	public PlatformBenchmarkResult(boolean completeSuccessfully) {
		this.completeSuccessfully = completeSuccessfully;
	}

	public boolean isCompleteSuccessfully() {
		return completeSuccessfully;
	}

	public void setCompleteSuccessfully(boolean completeSuccessfully) {
		this.completeSuccessfully = completeSuccessfully;
	}
}
