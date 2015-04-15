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
package nl.tudelft.graphalytics.mapreducev2.conn;

import nl.tudelft.graphalytics.mapreducev2.HadoopTestFolders;
import nl.tudelft.graphalytics.mapreducev2.HadoopTestUtils;
import nl.tudelft.graphalytics.validation.GraphStructure;
import nl.tudelft.graphalytics.validation.conn.ConnectedComponentsOutput;
import nl.tudelft.graphalytics.validation.conn.ConnectedComponentsValidationTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Tim Hegeman
 */
public class ConnectedComponentsTest extends ConnectedComponentsValidationTest {

	private static HadoopTestUtils hadoopTestUtils;

	@Rule
	public HadoopTestFolders testFolders = new HadoopTestFolders();

	@BeforeClass
	public static void setUp() throws IOException {
		hadoopTestUtils = new HadoopTestUtils();
		hadoopTestUtils.startCluster(ConnectedComponentsTest.class.getName());
	}

	@AfterClass
	public static void tearDown() {
		hadoopTestUtils.shutdownCluster();
	}

	@Override
	public ConnectedComponentsOutput executeDirectedConnectedComponents(GraphStructure graph) throws Exception {
		hadoopTestUtils.writeGraphToDirectory(graph, testFolders.getRawInputDirectory());
		hadoopTestUtils.convertGraphToHadoopFormat(testFolders.getRawInputDirectory(), testFolders.getInputDirectory());
		hadoopTestUtils.runMapReduceJob(new ConnectedComponentsJobLauncher(), true, null, testFolders);
		return parseOutput();
	}

	@Override
	public ConnectedComponentsOutput executeUndirectedConnectedComponents(GraphStructure graph) throws Exception {
		hadoopTestUtils.writeGraphToDirectory(graph, testFolders.getInputDirectory());
		hadoopTestUtils.runMapReduceJob(new ConnectedComponentsJobLauncher(), false, null, testFolders);
		return parseOutput();
	}

	private ConnectedComponentsOutput parseOutput() throws IOException {
		final Pattern WHITESPACE = Pattern.compile("[ \t]");
		final Pattern DOLLAR = Pattern.compile("\\$");

		List<String> outputData = hadoopTestUtils.readOutputAsLines(testFolders);
		Map<Long, Long> componentIds = new HashMap<>();
		for (String line : outputData) {
			String idAndValueString = DOLLAR.split(line, 2)[0];
			String[] idAndValueTokens = WHITESPACE.split(idAndValueString);

			long vertexId = Long.parseLong(idAndValueTokens[0]);
			long vertexValue = Long.parseLong(idAndValueTokens[1]);

			componentIds.put(vertexId, vertexValue);
		}
		return new ConnectedComponentsOutput(componentIds);
	}

}
