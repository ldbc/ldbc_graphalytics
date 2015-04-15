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
package nl.tudelft.graphalytics.neo4j.cd;

import nl.tudelft.graphalytics.neo4j.AbstractComputationTest;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static nl.tudelft.graphalytics.neo4j.cd.CommunityDetectionComputation.LABEL;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * Test case for the community detection implementation on Neo4j.
 *
 * @author Tim Hegeman
 */
public class CommunityDetectionComputationTest extends AbstractComputationTest {

	private static final float NODE_PREFERENCE = 0.1f;
	private static final float HOP_ATTENUATION = 0.1f;
	private static final int MAX_ITERATIONS = 5;

	private static List<List<Long>> parseOutputResource(String resourceName) throws IOException {
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
				CommunityDetectionComputationTest.class.getResourceAsStream(resourceName)))) {
			List<List<Long>> communities = new ArrayList<>();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				List<Long> community = new ArrayList<>();
				String[] tokens = line.split(" ");
				for (String vertexId : tokens) {
					community.add(Long.parseLong(vertexId));
				}
				communities.add(community);
			}
			return communities;
		}
	}

	@Test
	public void testExample() throws IOException {
		// Load data
		loadGraphFromResource("/test-examples/cd-input");
		// Execute algorithm
		new CommunityDetectionComputation(graphDatabase, NODE_PREFERENCE, HOP_ATTENUATION, MAX_ITERATIONS).run();
		// Verify output
		List<List<Long>> expectedOutput = parseOutputResource("/test-examples/cd-output");
		try (Transaction transaction = graphDatabase.beginTx()) {
			Set<Long> communityIds = new HashSet<>();
			for (List<Long> community : expectedOutput) {
				long communityHead = community.get(0);
				long communityId = (long) getNode(communityHead).getProperty(LABEL, Long.MAX_VALUE);
				assertThat("expected " + communityHead + " to be in a new community",
						communityId, not(isIn(communityIds)));
				communityIds.add(communityId);

				for (long communityMember : community) {
					long communityIdOfMember = (long) getNode(communityMember).getProperty(LABEL, Long.MIN_VALUE);
					assertThat("expected vertices " + communityHead + " and " + communityMember +
							" to be in the same community", communityIdOfMember, is(communityId));
				}
			}
		}
	}

}
