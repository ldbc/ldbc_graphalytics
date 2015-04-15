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
package nl.tudelft.graphalytics.mapreducev2;

import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import java.io.File;

/**
 * @author Tim Hegeman
 */
public class HadoopTestFolders extends ExternalResource {

	private TemporaryFolder temporaryFolder = new TemporaryFolder();
	private File rawInputDirectory;
	private File inputDirectory;
	private File intermediateDirectory;
	private File outputDirectory;

	@Override
	protected void before() throws Throwable {
		temporaryFolder.create();

		rawInputDirectory = new File(temporaryFolder.getRoot(), "raw-input");
		inputDirectory = new File(temporaryFolder.getRoot(), "input");
		intermediateDirectory = new File(temporaryFolder.getRoot(), "intermediate");
		outputDirectory = new File(temporaryFolder.getRoot(), "output");
	}

	@Override
	protected void after() {
		temporaryFolder.delete();
	}

	public File getRawInputDirectory() {
		return rawInputDirectory;
	}

	public File getInputDirectory() {
		return inputDirectory;
	}

	public File getIntermediateDirectory() {
		return intermediateDirectory;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

}
