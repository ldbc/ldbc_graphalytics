package nl.tudelft.graphalytics.giraph.cd;

import nl.tudelft.graphalytics.giraph.AbstractComputationTest;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.Computation;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.utils.TestGraph;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Writable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by tim on 2/8/15.
 */
public abstract class CommunityDetectionComputationTest<E extends Writable> extends
		AbstractComputationTest<CDLabel, E> {

	private static final float NODE_PREFERENCE = 0.1f;
	private static final float HOP_ATTENUATION = 0.1f;
	private static final int MAX_ITERATIONS = 5;

	private static Map<Long, Long> extractCommunityHeads(Map<Long, List<Long>> communities) {
		Map<Long, Long> communityHeads = new HashMap<>();
		for (long communityId : communities.keySet()) {
			communityHeads.put(communityId, communities.get(communityId).get(0));
		}
		return communityHeads;
	}

	private static Map<Long, Long> extractExpectedCommunityPerVertex(Map<Long, List<Long>> communities) {
		Map<Long, Long> vertexToCommunity = new HashMap<>();
		for (long communityId : communities.keySet()) {
			for (long communityMember : communities.get(communityId)) {
				vertexToCommunity.put(communityMember, communityId);
			}
		}
		return vertexToCommunity;
	}

	private static Map<String, Long> mapLabelsToCommunityIds(TestGraph<LongWritable, CDLabel, ?> result,
	                                                         Map<Long, Long> communityHeads) {
		Map<String, Long> labelToCommunityId = new HashMap<>();
		for (long communityId : communityHeads.keySet()) {
			Vertex<LongWritable, CDLabel, ?> communityHead =
					result.getVertex(new LongWritable(communityHeads.get(communityId)));
			labelToCommunityId.put(communityHead.getValue().getLabelName().toString(), communityId);
		}
		return labelToCommunityId;
	}

	protected void performTest(Class<? extends Computation> computationClass, String input, String output)
			throws Exception {
		GiraphConfiguration configuration = new GiraphConfiguration();
		configuration.setComputationClass(computationClass);
		CommunityDetectionConfiguration.HOP_ATTENUATION.set(configuration, HOP_ATTENUATION);
		CommunityDetectionConfiguration.MAX_ITERATIONS.set(configuration, MAX_ITERATIONS);
		CommunityDetectionConfiguration.NODE_PREFERENCE.set(configuration, NODE_PREFERENCE);

		TestGraph<LongWritable, CDLabel, ?> result =
				runTest(configuration, input);

		Map<Long, List<Long>> expectedCommunities = parseOutput(output);
		Set<Long> expectedVertices = new HashSet<>();
		for (List<Long> community : expectedCommunities.values()) {
			expectedVertices.addAll(community);
		}

		assertThat("result graph has the correct number of vertices",
				result.getVertices().keySet(), hasSize(expectedVertices.size()));

		Map<Long, Long> communityHeads = extractCommunityHeads(expectedCommunities);
		Map<Long, Long> vertexToExpectedCommunity = extractExpectedCommunityPerVertex(expectedCommunities);
		Map<String, Long> labelToCommunityId = mapLabelsToCommunityIds(result, communityHeads);

		for (Vertex<LongWritable, CDLabel, ?> vertex : result.getVertices().values()) {
			String label = vertex.getValue().getLabelName().toString();
			long communityId = labelToCommunityId.get(label);
			long vertexId = vertex.getId().get();
			long expectedCommunityId = vertexToExpectedCommunity.get(vertexId);
			assertThat("vertex " + vertexId + " is in the same community as " + communityHeads.get(expectedCommunityId),
					communityId, is(equalTo(expectedCommunityId)));
		}
	}

	private Map<Long, List<Long>> parseOutput(String outputResource) throws IOException {
		try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream(outputResource)))) {
			Map<Long, List<Long>> communities = new HashMap<>();
			long communityId = 1;
			String line;
			while ((line = outputReader.readLine()) != null) {
				String[] tokens = line.split(" ");
				communities.put(communityId, new ArrayList<Long>());
				for (String communityMember : tokens) {
					communities.get(communityId).add(Long.parseLong(communityMember));
				}
				communityId++;
			}
			return communities;
		}
	}

	@Override
	protected CDLabel getDefaultValue(long vertexId) {
		return new CDLabel(String.valueOf(vertexId), 1.0f);
	}

	@Override
	protected CDLabel parseValue(long vertexId, String value) {
		// Unused
		return null;
	}
}
