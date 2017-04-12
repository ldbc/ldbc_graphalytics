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
package science.atlarge.graphalytics.report;

import science.atlarge.graphalytics.report.result.BenchmarkSuiteResult;

/**
 * Created by tim on 12/14/15.
 */
public interface BenchmarkReportGenerator {

	/**
	 * @param result the results of running a benchmark suite from which a report is to be generated
	 */
	BenchmarkReport generateReportFromResults(BenchmarkSuiteResult result);

}
