package nl.tudelft.graphalytics.mapreducev2.bfs;

import nl.tudelft.graphalytics.domain.algorithms.BreadthFirstSearchParameters;
import nl.tudelft.graphalytics.mapreducev2.HadoopTestFolders;
import nl.tudelft.graphalytics.mapreducev2.HadoopTestUtils;
import nl.tudelft.graphalytics.validation.GraphStructure;
import nl.tudelft.graphalytics.validation.bfs.BreadthFirstSearchOutput;
import nl.tudelft.graphalytics.validation.bfs.BreadthFirstSearchValidationTest;
import org.junit.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Tim Hegeman
 */
public class BreadthFirstSearchTest extends BreadthFirstSearchValidationTest {

	private static HadoopTestUtils hadoopTestUtils;

	@Rule
	public HadoopTestFolders testFolders = new HadoopTestFolders();

	@BeforeClass
	public static void setUp() throws IOException {
		hadoopTestUtils = new HadoopTestUtils();
		hadoopTestUtils.startCluster(BreadthFirstSearchTest.class.getName());
	}

	@AfterClass
	public static void tearDown() {
		hadoopTestUtils.shutdownCluster();
	}

	@Override
	public BreadthFirstSearchOutput executeDirectedBreadthFirstSearch(GraphStructure graph,
			BreadthFirstSearchParameters parameters) throws Exception {
		hadoopTestUtils.writeGraphToDirectory(graph, testFolders.getRawInputDirectory());
		hadoopTestUtils.convertGraphToHadoopFormat(testFolders.getRawInputDirectory(), testFolders.getInputDirectory());
		hadoopTestUtils.runMapReduceJob(new BreadthFirstSearchJobLauncher(), true, parameters, testFolders);
		return parseOutput();
	}

	@Override
	public BreadthFirstSearchOutput executeUndirectedBreadthFirstSearch(GraphStructure graph,
			BreadthFirstSearchParameters parameters) throws Exception {
		hadoopTestUtils.writeGraphToDirectory(graph, testFolders.getInputDirectory());
		hadoopTestUtils.runMapReduceJob(new BreadthFirstSearchJobLauncher(), false, parameters, testFolders);
		return parseOutput();
	}

	private BreadthFirstSearchOutput parseOutput() throws IOException {
		final Pattern WHITESPACE = Pattern.compile("[ \t]");
		final Pattern DOLLAR = Pattern.compile("\\$");

		List<String> outputData = hadoopTestUtils.readOutputAsLines(testFolders);
		Map<Long, Long> pathLengths = new HashMap<>();
		for (String line : outputData) {
			String vertexIdString = WHITESPACE.split(line, 2)[0];
			long vertexId = Long.parseLong(vertexIdString);

			String[] valueTokens = DOLLAR.split(line);
			long vertexValue = Long.MAX_VALUE;
			if (valueTokens.length == 2) {
				vertexValue = Long.parseLong(valueTokens[1]);
			}

			pathLengths.put(vertexId, vertexValue);
		}
		return new BreadthFirstSearchOutput(pathLengths);
	}

}
