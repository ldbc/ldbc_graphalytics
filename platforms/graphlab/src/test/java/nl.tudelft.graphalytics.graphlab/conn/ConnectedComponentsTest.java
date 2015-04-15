package nl.tudelft.graphalytics.graphlab.conn;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.graphlab.AlgorithmTest;
import org.junit.Test;

/**
 * Test class for ConnectedComponents. Executes the CONN on a small graph,
 * and verifies that the output of the computation matches the expected results.
 *
 * @author Jorai Rijsdijk
 */
public class ConnectedComponentsTest extends AlgorithmTest {
    @Test
    public void testDirectedConnectedComponents() {
        performTest(Algorithm.CONN, "conn-dir", "conn/ConnectedComponentsTest.py", null, true, true);
    }

    @Test
    public void testUndirectedConnectedComponents() {
        performTest(Algorithm.CONN, "conn-undir", "conn/ConnectedComponentsTest.py", null, false, true);
    }
}
