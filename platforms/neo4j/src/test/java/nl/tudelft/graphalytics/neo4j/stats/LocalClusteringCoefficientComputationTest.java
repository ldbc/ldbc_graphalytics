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
package nl.tudelft.graphalytics.neo4j.stats;

import nl.tudelft.graphalytics.neo4j.AbstractComputationTest;
import nl.tudelft.graphalytics.neo4j.stats.LocalClusteringCoefficientComputation.LocalClusteringCoefficientResult;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

/**
 * Test case for the connected components implementation on Neo4j.
 *
 * @author Tim Hegeman
 */
public class LocalClusteringCoefficientComputationTest extends AbstractComputationTest {

	private static final long GLOBAL_ID = Long.MIN_VALUE;
	private static final double ERROR_BOUND = 1e-4;

	@Test
	public void testExample() throws IOException {
		// Load data
		loadGraphFromResource("/test-examples/stats-input");
		// Execute algorithm
		LocalClusteringCoefficientResult result = new LocalClusteringCoefficientComputation(graphDatabase).run();
		// Verify output
		Map<Long, Double> expectedOutput = parseOutputResource("/test-examples/stats-output");
		assertThat("incorrect mean LCC",
				result.getMeanLcc(), is(closeTo(expectedOutput.remove(GLOBAL_ID), ERROR_BOUND)));
		try (Transaction transaction = graphDatabase.beginTx()) {
			for (long vertexId : expectedOutput.keySet()) {
				double clusteringCoefficient = (double) getNode(vertexId).getProperty(
						LocalClusteringCoefficientComputation.LCC, Double.NaN);
				double expectedClusteringCoefficient = expectedOutput.get(vertexId);
				assertThat("incorrect clustering coefficient computed for id " + vertexId,
						clusteringCoefficient, is(closeTo(expectedClusteringCoefficient, ERROR_BOUND)));
			}
		}
	}

	private Map<Long, Double> parseOutputResource(String resourceName) throws IOException {
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
				LocalClusteringCoefficientComputationTest.class.getResourceAsStream(resourceName)))) {
			Map<Long, Double> expectedOutput = new HashMap<>();
			expectedOutput.put(GLOBAL_ID, Double.parseDouble(bufferedReader.readLine()));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] tokens = line.split(" ");
				expectedOutput.put(Long.parseLong(tokens[0]), Double.parseDouble(tokens[1]));
			}
			return expectedOutput;
		}
	}

}
