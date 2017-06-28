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
package science.atlarge.graphalytics.report;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A single file in a benchmark report. Defines a method that allows the file to be written to a report directory,
 * regardless of the implemented format (e.g., HTML, plain text, RDF).
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public interface BenchmarkReportFile {

	/**
	 * Writes this single benchmark report file to a directory. This function may create directories when needed and
	 * overwrite any previously existing file of the same name.
	 *
	 * @param reportPath the base directory to which the benchmark report is written
	 * @throws IOException if an IO error occurred
	 */
	void write(Path reportPath) throws IOException;

}
