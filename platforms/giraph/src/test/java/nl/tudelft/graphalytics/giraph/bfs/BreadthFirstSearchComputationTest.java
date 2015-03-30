package nl.tudelft.graphalytics.giraph.bfs;

import nl.tudelft.graphalytics.domain.algorithms.BreadthFirstSearchParameters;
import nl.tudelft.graphalytics.giraph.GiraphTestGraphLoader;
import nl.tudelft.graphalytics.validation.GraphStructure;
import nl.tudelft.graphalytics.validation.bfs.BreadthFirstSearchOutput;
import nl.tudelft.graphalytics.validation.bfs.BreadthFirstSearchValidationTest;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.utils.InternalVertexRunner;
import org.apache.giraph.utils.TestGraph;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tim Hegeman
 */
public class BreadthFirstSearchComputationTest extends BreadthFirstSearchValidationTest {

	@Override
	public BreadthFirstSearchOutput executeDirectedBreadthFirstSearch(
			GraphStructure graph, BreadthFirstSearchParameters parameters) throws Exception {
		GiraphConfiguration configuration = new GiraphConfiguration();
		configuration.setComputationClass(BreadthFirstSearchComputation.class);
		BreadthFirstSearchConfiguration.SOURCE_VERTEX.set(configuration, parameters.getSourceVertex());

		TestGraph<LongWritable, LongWritable, NullWritable> inputGraph =
				GiraphTestGraphLoader.createGraph(configuration, graph, new LongWritable(-1), NullWritable.get());

		TestGraph<LongWritable, LongWritable, NullWritable> result =
				InternalVertexRunner.runWithInMemoryOutput(configuration, inputGraph);

		Map<Long, Long> pathLengths = new HashMap<>();
		for (Map.Entry<LongWritable, Vertex<LongWritable, LongWritable, NullWritable>> vertexEntry :
				result.getVertices().entrySet()) {
			pathLengths.put(vertexEntry.getKey().get(), vertexEntry.getValue().getValue().get());
		}

		return new BreadthFirstSearchOutput(pathLengths);
	}

	@Override
	public BreadthFirstSearchOutput executeUndirectedBreadthFirstSearch(
			GraphStructure graph, BreadthFirstSearchParameters parameters) throws Exception {
		return executeDirectedBreadthFirstSearch(graph, parameters);
	}

}
