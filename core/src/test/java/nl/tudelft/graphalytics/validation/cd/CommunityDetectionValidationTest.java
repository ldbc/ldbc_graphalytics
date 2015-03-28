package nl.tudelft.graphalytics.validation.cd;

import nl.tudelft.graphalytics.domain.algorithms.CommunityDetectionParameters;
import nl.tudelft.graphalytics.validation.GraphStructure;
import nl.tudelft.graphalytics.validation.io.GraphParser;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author Tim Hegeman
 */
public abstract class CommunityDetectionValidationTest {

	private static Collection<Set<Long>> loadCommunitiesFromStream(InputStream communityDataStream) throws IOException {
		try (BufferedReader communitiesReader = new BufferedReader(new InputStreamReader(communityDataStream))) {
			Collection<Set<Long>> communities = new ArrayList<>();
			for (String line = communitiesReader.readLine(); line != null; line = communitiesReader.readLine()) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}

				String tokens[] = line.split(" ");
				Set<Long> community = new HashSet<>();
				for (String token : tokens) {
					community.add(Long.parseLong(token));
				}
				communities.add(community);
			}
			return communities;
		}
	}

	public abstract CommunityDetectionOutput executeDirectedCommunityDetection(
			GraphStructure graph, CommunityDetectionParameters parameters) throws Exception;

	public abstract CommunityDetectionOutput executeUndirectedCommunityDetection(
			GraphStructure graph, CommunityDetectionParameters parameters) throws Exception;

	@Test
	public final void testDirectedCommunityDetectionOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/cd-dir-input";
		final String outputPath = "/validation-graphs/cd-dir-output";
		final float nodePreference = 0.1f;
		final float hopAttenuation = 0.1f;
		final int maxIterations = 5;

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), true);

		CommunityDetectionParameters parameters =
				new CommunityDetectionParameters(nodePreference, hopAttenuation, maxIterations);
		CommunityDetectionOutput executionResult = executeDirectedCommunityDetection(inputGraph, parameters);

		validateCommunityDetection(executionResult, outputPath);
	}

	@Test
	public final void testUndirectedCommunityDetectionOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/cd-undir-input";
		final String outputPath = "/validation-graphs/cd-undir-output";
		final float nodePreference = 0.1f;
		final float hopAttenuation = 0.1f;
		final int maxIterations = 5;

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), false);

		CommunityDetectionParameters parameters =
				new CommunityDetectionParameters(nodePreference, hopAttenuation, maxIterations);
		CommunityDetectionOutput executionResult = executeUndirectedCommunityDetection(inputGraph, parameters);

		validateCommunityDetection(executionResult, outputPath);
	}

	private void validateCommunityDetection(CommunityDetectionOutput executionResult, String outputPath)
			throws IOException {
		Collection<Set<Long>> expectedCommunities = loadCommunitiesFromStream(
				getClass().getResourceAsStream(outputPath));
		Set<Long> expectedVertices = extractExpectedVertices(expectedCommunities);

		assertThat("result graph has the correct number of vertices",
				executionResult.getVertices(), hasSize(expectedVertices.size()));
		assertThat("result graph has the correct number of communities",
				executionResult.getCommunities(), hasSize(expectedCommunities.size()));
		for (Set<Long> expectedCommunity : expectedCommunities) {
			long vertexInCommunity = expectedCommunity.iterator().next();
			long communityId = executionResult.getCommunityIdForVertex(vertexInCommunity);
			Set<Long> community = executionResult.getVerticesInCommunity(communityId);
			assertThat("community " + communityId + " consists of the expected vertices",
					community, containsInAnyOrder(expectedCommunity.toArray()));
		}
	}

	private static Set<Long> extractExpectedVertices(Collection<Set<Long>> expectedCommunities) {
		Set<Long> expectedVertices = new HashSet<>();
		for (Set<Long> community : expectedCommunities) {
			for (Long vertex : community) {
				expectedVertices.add(vertex);
			}
		}
		return expectedVertices;
	}

}
