package nl.tudelft.graphalytics.giraph.cd;

import org.apache.hadoop.io.BooleanWritable;
import org.junit.Test;

/**
 * Created by tim on 2/8/15.
 */
public class DirectedCommunityDetectionComputationTest extends CommunityDetectionComputationTest<BooleanWritable> {

	@Test
	public void testDirectedExample() throws Exception {
		performTest(DirectedCommunityDetectionComputation.class,
				"/test-examples/cd-dir-input",
				"/test-examples/cd-dir-output");
	}

	@Override
	protected BooleanWritable getDefaultEdgeValue(long sourceId, long destinationId) {
		return new BooleanWritable(false);
	}
}
