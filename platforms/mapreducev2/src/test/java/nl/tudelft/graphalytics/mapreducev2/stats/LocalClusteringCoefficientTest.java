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
package nl.tudelft.graphalytics.mapreducev2.stats;

import nl.tudelft.graphalytics.mapreducev2.HadoopTestFolders;
import nl.tudelft.graphalytics.mapreducev2.HadoopTestUtils;
import nl.tudelft.graphalytics.validation.GraphStructure;
import nl.tudelft.graphalytics.validation.conn.ConnectedComponentsOutput;
import nl.tudelft.graphalytics.validation.stats.LocalClusteringCoefficientOutput;
import nl.tudelft.graphalytics.validation.stats.LocalClusteringCoefficientValidationTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Tim Hegeman
 */
public class LocalClusteringCoefficientTest extends LocalClusteringCoefficientValidationTest {

	private static HadoopTestUtils hadoopTestUtils;

	@Rule
	public HadoopTestFolders testFolders = new HadoopTestFolders();

	@BeforeClass
	public static void setUp() throws IOException {
		hadoopTestUtils = new HadoopTestUtils();
		hadoopTestUtils.startCluster(LocalClusteringCoefficientTest.class.getName());
	}

	@AfterClass
	public static void tearDown() {
		hadoopTestUtils.shutdownCluster();
	}

	@Override
	public LocalClusteringCoefficientOutput executeDirectedLocalClusteringCoefficient(GraphStructure graph)
			throws Exception {
		hadoopTestUtils.writeGraphToDirectory(graph, testFolders.getRawInputDirectory());
		hadoopTestUtils.convertGraphToHadoopFormat(testFolders.getRawInputDirectory(), testFolders.getInputDirectory());
		hadoopTestUtils.runMapReduceJob(new STATSJobLauncher(), true, null, testFolders);
		return parseOutput();
	}

	@Override
	public LocalClusteringCoefficientOutput executeUndirectedLocalClusteringCoefficient(GraphStructure graph)
			throws Exception {
		hadoopTestUtils.writeGraphToDirectory(graph, testFolders.getInputDirectory());
		hadoopTestUtils.runMapReduceJob(new STATSJobLauncher(), false, null, testFolders);
		return parseOutput();
	}

	private LocalClusteringCoefficientOutput parseOutput() throws IOException {
		final Pattern WHITESPACE = Pattern.compile("[ \t]");

		List<String> outputData = hadoopTestUtils.readOutputAsLines(testFolders);
		Map<Long, Double> clusteringCoefficients = new HashMap<>();
		double meanClusteringCoefficient = Double.NaN;
		for (String line : outputData) {
			String[] idAndValueTokens = WHITESPACE.split(line);

			assertThat("each line of output contains exactly two fields",
					idAndValueTokens.length, is(equalTo(2)));

			if (idAndValueTokens[0].equals("MEAN")) {
				meanClusteringCoefficient = Double.parseDouble(idAndValueTokens[1]);
			} else {
				clusteringCoefficients.put(Long.parseLong(idAndValueTokens[0]),
						Double.parseDouble(idAndValueTokens[1]));
			}
		}
		return new LocalClusteringCoefficientOutput(clusteringCoefficients, meanClusteringCoefficient);
	}

}
