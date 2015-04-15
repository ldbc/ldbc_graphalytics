package nl.tudelft.graphalytics.graphlab.stats;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.graphlab.AlgorithmTest;
import org.junit.Test;

/**
 * Test class for LocalClusteringCoefficient. Executes the STATS on a small graph,
 * and verifies that the output of the computation matches the expected results.
 *
 * @author Jorai Rijsdijk
 */
public class LocalClusteringCoefficientTest extends AlgorithmTest {
    @Test
    public void testDirectedLocalClusteringCoefficient() {
        performTest(Algorithm.STATS, "stats-dir", "stats/LocalClusteringCoefficientTest.py", null, true, true);
    }

    @Test
    public void testUndirectedLocalClusteringCoefficient() {
        performTest(Algorithm.STATS, "stats-undir", "stats/LocalClusteringCoefficientTest.py", null, false, true);
    }
}
