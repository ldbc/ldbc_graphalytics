/*
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
package nl.tudelft.graphalytics.validation.algorithms.cd;

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
 * Framework for validating the output of an implementation of the community detection algorithm. Defines two functions
 * to be implemented to run a platform-specific community detection implementation on an in-memory graph.
 *
 * @author Tim Hegeman
 */
public abstract class CommunityDetectionValidationTest {

	/**
	 * Parses a stream containing community detection output data. The format is a single line per community, with the
	 * vertex ids of the community separated by spaces.
	 *
	 * @param communityDataStream a stream containing the community detection output data
	 * @return a collection of communities, represented as a set of vertex ids per community
	 * @throws IOException iff the stream could not be read
	 */
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

	/**
	 * Executes the platform-specific implementation of the community detection algorithm on an in-memory directed
	 * graph with given parameters, and returns the output of the execution.
	 * <p/>
	 * This function is called with sample graphs and the output is compared with known-correct results.
	 *
	 * @param graph      the graph to execute the community detection algorithm on
	 * @param parameters the values for the algorithm parameters to use
	 * @return the output of the community detection algorithm
	 * @throws Exception
	 */
	public abstract CommunityDetectionOutput executeDirectedCommunityDetection(
			GraphStructure graph, CommunityDetectionParameters parameters) throws Exception;

	/**
	 * Executes the platform-specific implementation of the community detection algorithm on an in-memory undirected
	 * graph with given parameters, and returns the output of the execution.
	 * <p/>
	 * This function is called with sample graphs and the output is compared with known-correct results.
	 *
	 * @param graph      the graph to execute the community detection algorithm on
	 * @param parameters the values for the algorithm parameters to use
	 * @return the output of the community detection algorithm
	 * @throws Exception
	 */
	public abstract CommunityDetectionOutput executeUndirectedCommunityDetection(
			GraphStructure graph, CommunityDetectionParameters parameters) throws Exception;

	@Test
	public final void testDirectedCommunityDetectionOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/cd-dir-input";
		final String outputPath = "/validation-graphs/cd-dir-output";
		final int maxIterations = 5;

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), true);

		CommunityDetectionParameters parameters = new CommunityDetectionParameters(maxIterations);
		CommunityDetectionOutput executionResult = executeDirectedCommunityDetection(inputGraph, parameters);

		validateCommunityDetection(executionResult, outputPath);
	}

	@Test
	public final void testUndirectedCommunityDetectionOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/cd-undir-input";
		final String outputPath = "/validation-graphs/cd-undir-output";
		final int maxIterations = 5;

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), false);

		CommunityDetectionParameters parameters = new CommunityDetectionParameters(maxIterations);
		CommunityDetectionOutput executionResult = executeUndirectedCommunityDetection(inputGraph, parameters);

		validateCommunityDetection(executionResult, outputPath);
	}

	/**
	 * Validates the output of a community detection implementation. The output is compared with known results in a
	 * separate file.
	 *
	 * @param executionResult the result of the breadth-first search execution
	 * @param outputPath      the output file to read the correct results from
	 * @throws IOException iff the output file could not be loaded
	 */
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

	/**
	 * @param expectedCommunities a collection of communities
	 * @return a set of all vertices that are part of any community
	 */
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
