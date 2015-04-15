package nl.tudelft.graphalytics.graphlab.bfs;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.algorithms.BreadthFirstSearchParameters;
import nl.tudelft.graphalytics.graphlab.AlgorithmTest;
import org.junit.Test;

/**
 * Test class for BreadthFirstSearch. Executes the BFS on a small graph,
 * and verifies that the output of the computation matches the expected results.
 *
 * @author Jorai Rijsdijk
 */
public class BreadthFirstSearchTest extends AlgorithmTest {
    @Test
    public void testBFS() {
        performTest(Algorithm.BFS, "bfs", "bfs/BreadthFirstSearchTest.py", new BreadthFirstSearchParameters(1), true, true);
    }
}
