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
package nl.tudelft.graphalytics.giraph.cd;

import nl.tudelft.graphalytics.domain.algorithms.CommunityDetectionParameters;
import nl.tudelft.graphalytics.giraph.GiraphTestGraphLoader;
import nl.tudelft.graphalytics.validation.GraphStructure;
import nl.tudelft.graphalytics.validation.cd.CommunityDetectionOutput;
import nl.tudelft.graphalytics.validation.cd.CommunityDetectionValidationTest;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.Computation;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.utils.InternalVertexRunner;
import org.apache.giraph.utils.TestGraph;
import org.apache.hadoop.io.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for validating the Giraph community detection implementation using Graphalytics' validation framework.
 *
 * @author Tim Hegeman
 */
public class CommunityDetectionComputationTest extends CommunityDetectionValidationTest {

	private static GiraphConfiguration configurationFromParameters(Class<? extends Computation> computationClass,
			CommunityDetectionParameters parameters) {
		GiraphConfiguration configuration = new GiraphConfiguration();
		configuration.setComputationClass(computationClass);
		CommunityDetectionConfiguration.HOP_ATTENUATION.set(configuration, parameters.getHopAttenuation());
		CommunityDetectionConfiguration.MAX_ITERATIONS.set(configuration, parameters.getMaxIterations());
		CommunityDetectionConfiguration.NODE_PREFERENCE.set(configuration, parameters.getNodePreference());
		return configuration;
	}

	private static <E extends Writable> CommunityDetectionOutput outputFromResultGraph(
			TestGraph<LongWritable, CDLabel, E> result) {
		Map<Long, Long> communityIds = new HashMap<>();
		for (Map.Entry<LongWritable, Vertex<LongWritable, CDLabel, E>> vertexEntry :
				result.getVertices().entrySet()) {
			Text label = vertexEntry.getValue().getValue().getLabelName();
			communityIds.put(vertexEntry.getKey().get(), Long.parseLong(label.toString()));
		}

		return new CommunityDetectionOutput(communityIds);
	}

	@Override
	public CommunityDetectionOutput executeDirectedCommunityDetection(
			GraphStructure graph, CommunityDetectionParameters parameters) throws Exception {
		GiraphConfiguration configuration = configurationFromParameters(DirectedCommunityDetectionComputation.class,
				parameters);

		TestGraph<LongWritable, CDLabel, BooleanWritable> inputGraph =
				GiraphTestGraphLoader.createGraph(configuration, graph, new CDLabel(), new BooleanWritable());

		TestGraph<LongWritable, CDLabel, BooleanWritable> result =
				InternalVertexRunner.runWithInMemoryOutput(configuration, inputGraph);

		return outputFromResultGraph(result);
	}

	@Override
	public CommunityDetectionOutput executeUndirectedCommunityDetection(
			GraphStructure graph, CommunityDetectionParameters parameters) throws Exception {
		GiraphConfiguration configuration = configurationFromParameters(UndirectedCommunityDetectionComputation.class,
				parameters);

		TestGraph<LongWritable, CDLabel, NullWritable> inputGraph =
				GiraphTestGraphLoader.createGraph(configuration, graph, new CDLabel(), NullWritable.get());

		TestGraph<LongWritable, CDLabel, NullWritable> result =
				InternalVertexRunner.runWithInMemoryOutput(configuration, inputGraph);

		return outputFromResultGraph(result);
	}
}
