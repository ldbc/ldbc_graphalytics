package nl.tudelft.graphalytics.giraph.conn;

import nl.tudelft.graphalytics.giraph.GiraphTestGraphLoader;
import nl.tudelft.graphalytics.validation.GraphStructure;
import nl.tudelft.graphalytics.validation.conn.ConnectedComponentsOutput;
import nl.tudelft.graphalytics.validation.conn.ConnectedComponentsValidationTest;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.Computation;
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
public class ConnectedComponentsComputationTest extends ConnectedComponentsValidationTest {

	private static ConnectedComponentsOutput executeConnectedComponents(Class<? extends Computation> computationClass,
			GraphStructure graph) throws Exception {
		GiraphConfiguration configuration = new GiraphConfiguration();
		configuration.setComputationClass(computationClass);

		TestGraph<LongWritable, LongWritable, NullWritable> inputGraph =
				GiraphTestGraphLoader.createGraph(configuration, graph, new LongWritable(-1), NullWritable.get());

		TestGraph<LongWritable, LongWritable, NullWritable> result =
				InternalVertexRunner.runWithInMemoryOutput(configuration, inputGraph);

		Map<Long, Long> pathLengths = new HashMap<>();
		for (Map.Entry<LongWritable, Vertex<LongWritable, LongWritable, NullWritable>> vertexEntry :
				result.getVertices().entrySet()) {
			pathLengths.put(vertexEntry.getKey().get(), vertexEntry.getValue().getValue().get());
		}

		return new ConnectedComponentsOutput(pathLengths);
	}

	@Override
	public ConnectedComponentsOutput executeDirectedConnectedComponents(GraphStructure graph) throws Exception {
		return executeConnectedComponents(DirectedConnectedComponentsComputation.class, graph);
	}

	@Override
	public ConnectedComponentsOutput executeUndirectedConnectedComponents(GraphStructure graph) throws Exception {
		return executeConnectedComponents(UndirectedConnectedComponentsComputation.class, graph);
	}

}
