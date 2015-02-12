package nl.tudelft.graphalytics.graphlab.bfs;

import nl.tudelft.graphalytics.PlatformExecutionException;
import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.Graph;
import nl.tudelft.graphalytics.domain.GraphFormat;
import nl.tudelft.graphalytics.domain.algorithms.BreadthFirstSearchParameters;
import nl.tudelft.graphalytics.graphlab.GraphLabPlatform;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.fail;

/**
 * Test class for BreadthFirstSearch. Executes the BFS on a small graph,
 * and verifies that the output of the computation matches the expected results.
 *
 * @author Jorai Rijsdijk
 */
public class BreadthFirstSearchTest {
    private static GraphLabPlatform graphLab;
    private static final String BASE_PATH = BreadthFirstSearchTest.class.getResource("/").getPath();

    @BeforeClass
    public static void beforeClass() {
        graphLab = new GraphLabPlatform();
        graphLab.setSaveGraphResult(true);

    }

    @Test
    public void testBFS() {
        String graphFile = "test-examples/bfs-input";
        Graph graph = new Graph("bfs-input", graphFile, new GraphFormat(true, true));
        try {
            graphLab.uploadGraph(graph, BASE_PATH + graphFile);
        } catch (Exception e) {
            fail("Unable to upload graph" + (e.getMessage() != null ? ": " + e.getMessage() : ""));
            e.printStackTrace();
        }

        try {
            graphLab.executeAlgorithmOnGraph(Algorithm.BFS, graph, new BreadthFirstSearchParameters(1));
        } catch (PlatformExecutionException e) {
            fail("Algorithm execution failed" + (e.getMessage() != null ? ": " + e.getMessage(): ""));
            e.printStackTrace();
        }

        File testScriptFile = new File(BASE_PATH, "nl/tudelft/graphalytics/graphlab/bfs/BreadthFirstSearchTest.py");
        if (!testScriptFile.exists()) {
            fail("Cannot find GraphLab  BreadthFirstSearchTest.py script: " + testScriptFile.getAbsolutePath());
        }

        CommandLine commandLine = new CommandLine("python");
        commandLine.addArgument(testScriptFile.getAbsolutePath());
        commandLine.addArgument("target/bfs_" + graph.getName());
        commandLine.addArgument(BASE_PATH + "test-examples/bfs-output");

        // Set the executor of the command, if desired this can be changed to a custom implementation
        DefaultExecutor executor = new DefaultExecutor();

        // Set the OutputStream to enable printing the output of the algorithm
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(outputStream));

        try {
            // Execute the actual command and store the return code
            executor.execute(commandLine);
            // Print the command output
            System.out.println(outputStream.toString());
        } catch (IOException e) {
            // Catch the exception thrown when the process exits with result != 0 or another IOException occurs
            fail(outputStream.toString());
        }
    }
}
