package nl.tudelft.graphalytics.giraph.stats;

import nl.tudelft.graphalytics.giraph.AbstractComputationTest;
import org.apache.giraph.aggregators.AggregatorWriter;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.conf.ImmutableClassesGiraphConfiguration;
import org.apache.giraph.graph.Computation;
import org.apache.giraph.utils.TestGraph;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static nl.tudelft.graphalytics.giraph.stats.LocalClusteringCoefficientMasterComputation.LCC_AGGREGATOR_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Test class for computing the local clustering coefficient. Executes the Giraph implementation of the LCC computation
 * on a small graph, and verifies that the output of the computation matches the expected results.
 *
 * @author Tim Hegeman
 */
public class LocalClusteringCoefficientComputationTest extends AbstractComputationTest<DoubleWritable, NullWritable> {

	private static final double ERROR_BOUND = 1e-4;

	@Test
	public void testDirectedExample() throws Exception {
		performTest(DirectedLocalClusteringCoefficientComputation.class,
				"/test-examples/stats-dir-input",
				"/test-examples/stats-dir-output");
	}

	@Test
	public void testUndirectedExample() throws Exception {
		performTest(DirectedLocalClusteringCoefficientComputation.class,
				"/test-examples/stats-undir-input",
				"/test-examples/stats-undir-output");
	}

	private void performTest(Class<? extends Computation> computationClass, String input, String output)
			throws Exception {
		GiraphConfiguration configuration = new GiraphConfiguration();
		configuration.setComputationClass(computationClass);
		configuration.setMasterComputeClass(LocalClusteringCoefficientMasterComputation.class);
		configuration.setAggregatorWriterClass(InMemoryAggregatorWriter.class);

		TestGraph<LongWritable, DoubleWritable, NullWritable> result =
				runTest(configuration, input);
		TestGraph<LongWritable, DoubleWritable, NullWritable> expected;
		double expectedLcc;
		try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream(output)))) {
			expectedLcc = Double.parseDouble(outputReader.readLine());
			expected = parseGraphValues(configuration, outputReader);
		}

		assertThat("result graph has the correct number of vertices",
				result.getVertices().keySet(), hasSize(expected.getVertices().size()));
		for (LongWritable vertexId : result.getVertices().keySet())
			assertThat("vertex " + vertexId + " has correct value",
					result.getVertex(vertexId).getValue().get(),
					is(closeTo(expected.getVertex(vertexId).getValue().get(), ERROR_BOUND)));
		assertThat("mean local clustering coefficient is correct",
				((DoubleAverage) InMemoryAggregatorWriter.getAggregatedValues().get(LCC_AGGREGATOR_NAME)).get(),
				is(closeTo(expectedLcc, ERROR_BOUND)));
	}

	@Override
	protected DoubleWritable getDefaultValue(long vertexId) {
		return new DoubleWritable(0.0);
	}

	@Override
	protected NullWritable getDefaultEdgeValue(long sourceId, long destinationId) {
		return NullWritable.get();
	}

	@Override
	protected DoubleWritable parseValue(long vertexId, String value) {
		return new DoubleWritable(Double.parseDouble(value));
	}

	public static class InMemoryAggregatorWriter implements AggregatorWriter {

		private static final Map<String, Writable> aggregatedValues = new HashMap<>();

		public static Map<String, Writable> getAggregatedValues() {
			return aggregatedValues;
		}

		public static void resetAggregator() {
			aggregatedValues.clear();
		}

		@Override
		public void initialize(Mapper.Context context, long applicationAttempt) throws IOException {
			// Ignored
		}

		@Override
		public void writeAggregator(Iterable<Map.Entry<String, Writable>> aggregatorMap,
		                            long superstep) throws IOException {
			if (superstep == AggregatorWriter.LAST_SUPERSTEP)
				for (Map.Entry<String, Writable> aggregator : aggregatorMap)
					aggregatedValues.put(aggregator.getKey(), aggregator.getValue());
		}

		@Override
		public void close() throws IOException {
			// Ignored
		}

		@Override
		public ImmutableClassesGiraphConfiguration getConf() {
			return null;
		}

		@Override
		public void setConf(ImmutableClassesGiraphConfiguration configuration) {
			// Ignored
		}
	}

}
