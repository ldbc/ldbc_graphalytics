/**
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
package nl.tudelft.graphalytics.reporting;

import java.io.IOException;

/**
 * A single page in a benchmark report. Defines a method that allows the page to be written to a file,
 * regardless of the implemented format (e.g., HTML, plain text, RDF).
 *
 * @author Tim Hegeman
 */
public interface BenchmarkReportPage {

	/**
	 * Writes this single benchmark report page to a file. This function creates directories when needed and
	 * overwrites any previously existing file of the same name.
	 *
	 * @param reportPath the base directory to which the benchmark report is written
	 * @throws IOException if an IO error occurred
	 */
	public void write(String reportPath) throws IOException;

}
