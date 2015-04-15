package nl.tudelft.graphalytics.graphlab.cd;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.algorithms.CommunityDetectionParameters;
import nl.tudelft.graphalytics.graphlab.AlgorithmTest;
import org.junit.Test;

/**
 * Test class for CommunityDetection. Executes the CD on a small graph,
 * and verifies that the output of the computation matches the expected results.
 * @author Jorai Rijsdijk
 */
public class CommunityDetectionTest extends AlgorithmTest {
    @Test
    public void testCDDirected() {
        performTest(Algorithm.CD, "cd-dir", "cd/CommunityDetectionTest.py", new CommunityDetectionParameters(0.1f, 0.1f, 5), true, true);
    }

    @Test
    public void testCDUndirected() {
        performTest(Algorithm.CD, "cd-undir", "cd/CommunityDetectionTest.py", new CommunityDetectionParameters(0.1f, 0.1f, 5), false, true);
    }
}
