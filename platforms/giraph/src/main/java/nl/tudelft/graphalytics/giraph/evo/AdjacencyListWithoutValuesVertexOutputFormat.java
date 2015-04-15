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
package nl.tudelft.graphalytics.giraph.evo;

import java.io.IOException;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.io.formats.TextVertexOutputFormat;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * VertexOutputFormat for writing back graph structure without values. The output format is:
 * vertexId neighbour1 neighbour2 ...
 *
 * @author Tim Hegeman
 */
public class AdjacencyListWithoutValuesVertexOutputFormat extends TextVertexOutputFormat<LongWritable, Writable, Writable> {

	@Override
	public TextVertexWriter createVertexWriter(TaskAttemptContext context)
			throws IOException, InterruptedException {
		return new AdjacencyListWriter();
	}
	
	private class AdjacencyListWriter extends TextVertexWriterToEachLine {

		@Override
		protected Text convertVertexToLine(Vertex<LongWritable, Writable, Writable> vertex)
				throws IOException {
			StringBuffer sb = new StringBuffer(vertex.getId().toString());
			for (Edge<LongWritable, Writable> edge : vertex.getEdges()) {
				sb.append(' ').append(edge.getTargetVertexId());
			}
			return new Text(sb.toString());
		}
		
	}

}
