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
