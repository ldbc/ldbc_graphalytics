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
package nl.tudelft.graphalytics.giraph;

import nl.tudelft.graphalytics.validation.GraphStructure;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.utils.TestGraph;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Writable;

/**
 * @author Tim Hegeman
 */
public class GiraphTestGraphLoader {

	public static <V extends Writable, E extends Writable> TestGraph<LongWritable, V, E> createGraph(
			GiraphConfiguration configuration, GraphStructure input, V vertexValue, E edgeValue) {
		TestGraph<LongWritable, V, E> graph = new TestGraph<>(configuration);

		for (long sourceId : input.getVertices()) {
			graph.addVertex(new LongWritable(sourceId), vertexValue);
		}

		for (long sourceId : input.getVertices()) {
			for (long destinationId : input.getEdgesForVertex(sourceId)) {
				graph.addEdge(new LongWritable(sourceId), new LongWritable(destinationId), edgeValue);
			}
		}

		return graph;
	}

}
