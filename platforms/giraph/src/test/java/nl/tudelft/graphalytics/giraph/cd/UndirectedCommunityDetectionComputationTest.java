package nl.tudelft.graphalytics.giraph.cd;

import org.apache.hadoop.io.NullWritable;
import org.junit.Test;

/**
 * Created by tim on 2/8/15.
 */
public class UndirectedCommunityDetectionComputationTest extends CommunityDetectionComputationTest<NullWritable> {

	@Test
	public void testUndirectedExample() throws Exception {
		performTest(UndirectedCommunityDetectionComputation.class,
				"/test-examples/cd-undir-input",
				"/test-examples/cd-undir-output");
	}

	@Override
	protected NullWritable getDefaultEdgeValue(long sourceId, long destinationId) {
		return NullWritable.get();
	}
}
