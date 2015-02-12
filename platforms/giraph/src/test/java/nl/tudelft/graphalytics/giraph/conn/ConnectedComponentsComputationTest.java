package nl.tudelft.graphalytics.giraph.conn;

import nl.tudelft.graphalytics.giraph.AbstractComputationTest;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.Computation;
import org.apache.giraph.utils.TestGraph;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Test class for the connected components algorithm. Executes the Giraph implementation of the CC computation on a
 * small graph, and verifies that the output of the computation matches the expected results.
 *
 * @author Tim Hegeman
 */
public class ConnectedComponentsComputationTest extends AbstractComputationTest<LongWritable, NullWritable> {

	@Test
	public void testDirectedExample() throws Exception {
		performTest(DirectedConnectedComponentsComputation.class,
				"/test-examples/conn-dir-input",
				"/test-examples/conn-dir-output");
	}

	@Test
	public void testUndirectedExample() throws Exception {
		performTest(UndirectedConnectedComponentsComputation.class,
				"/test-examples/conn-undir-input",
				"/test-examples/conn-undir-output");
	}

	private void performTest(Class<? extends Computation> computationClass, String input, String output)
			throws Exception {
		GiraphConfiguration configuration = new GiraphConfiguration();
		configuration.setComputationClass(computationClass);

		TestGraph<LongWritable, LongWritable, NullWritable> result =
				runTest(configuration, input);
		TestGraph<LongWritable, LongWritable, NullWritable> expected =
				parseGraphValues(configuration, output);

		assertThat("result graph has the correct number of vertices",
				result.getVertices().keySet(), hasSize(expected.getVertices().size()));
		for (LongWritable vertexId : result.getVertices().keySet())
			assertThat("vertex " + vertexId + " has correct value",
					result.getVertex(vertexId).getValue(), is(equalTo(expected.getVertex(vertexId).getValue())));
	}

	@Override
	protected LongWritable getDefaultValue(long vertexId) {
		return new LongWritable(vertexId);
	}

	@Override
	protected NullWritable getDefaultEdgeValue(long sourceId, long destinationId) {
		return NullWritable.get();
	}

	@Override
	protected LongWritable parseValue(long vertexId, String value) {
		return new LongWritable(Long.parseLong(value));
	}
}
