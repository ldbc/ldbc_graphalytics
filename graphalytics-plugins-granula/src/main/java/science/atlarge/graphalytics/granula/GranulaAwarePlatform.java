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
package science.atlarge.graphalytics.granula;

import nl.tudelft.granula.modeller.job.JobModel;
import science.atlarge.graphalytics.execution.Platform;
import science.atlarge.graphalytics.report.result.BenchmarkResult;

import java.nio.file.Path;

/**
 * @author Tim Hegeman
 */
public interface GranulaAwarePlatform extends Platform {

	JobModel getJobModel();
	void enrichMetrics(BenchmarkResult benchmarkResult, Path arcDirectory);
}
