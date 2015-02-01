package nl.tudelft.graphalytics.neo4j.cd;

import nl.tudelft.graphalytics.neo4j.AbstractComputationTest;
import org.junit.Assert;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static nl.tudelft.graphalytics.neo4j.cd.CommunityDetectionComputation.LABEL;
import static org.hamcrest.CoreMatchers.is;

/**
 * Test case for the community detection implementation on Neo4j.
 *
 * @author Tim Hegeman
 */
public class CommunityDetectionComputationTest extends AbstractComputationTest {

	private static final float NODE_PREFERENCE = 0.1f;
	private static final float HOP_ATTENUATION = 0.1f;
	private static final int MAX_ITERATIONS = 5;

	@Test
	public void testExample() throws IOException {
		// Load data
		loadGraphFromResource("/test-examples/cd-input");
		// Execute algorithm
		new CommunityDetectionComputation(graphDatabase, NODE_PREFERENCE, HOP_ATTENUATION, MAX_ITERATIONS).run();
		// Verify output
		List<List<Long>> expectedOutput = parseOutputResource("/test-examples/cd-output");
		try (Transaction transaction = graphDatabase.beginTx()) {
			for (List<Long> community : expectedOutput) {
				long communityHead = community.get(0);
				long communityId = (long)getNode(communityHead).getProperty(LABEL, Long.MAX_VALUE);

				for (long communityMember : community) {
					long communityIdOfMember = (long)getNode(communityMember).getProperty(LABEL, Long.MIN_VALUE);
					Assert.assertThat("expected vertices " + communityHead + " and " + communityMember +
							" to be in the same community",	communityIdOfMember, is(communityId));
				}
			}
		}
	}

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

}
