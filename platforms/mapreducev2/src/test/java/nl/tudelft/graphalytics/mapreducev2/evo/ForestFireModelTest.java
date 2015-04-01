package nl.tudelft.graphalytics.mapreducev2.evo;

import nl.tudelft.graphalytics.domain.algorithms.ForestFireModelParameters;
import nl.tudelft.graphalytics.mapreducev2.HadoopTestFolders;
import nl.tudelft.graphalytics.mapreducev2.HadoopTestUtils;
import nl.tudelft.graphalytics.validation.GraphStructure;
import nl.tudelft.graphalytics.validation.evo.ForestFireModelValidationTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Tim Hegeman
 */
public class ForestFireModelTest extends ForestFireModelValidationTest {

	private static HadoopTestUtils hadoopTestUtils;

	@Rule
	public HadoopTestFolders testFolders = new HadoopTestFolders();

	@BeforeClass
	public static void setUp() throws IOException {
		hadoopTestUtils = new HadoopTestUtils();
		hadoopTestUtils.startCluster(ForestFireModelTest.class.getName());
	}

	@AfterClass
	public static void tearDown() {
		hadoopTestUtils.shutdownCluster();
	}

	@Override
	public GraphStructure executeDirectedForestFireModel(GraphStructure graph, ForestFireModelParameters parameters)
			throws Exception {
		hadoopTestUtils.writeGraphToDirectory(graph, testFolders.getRawInputDirectory());
		hadoopTestUtils.convertGraphToHadoopFormat(testFolders.getRawInputDirectory(), testFolders.getInputDirectory());
		hadoopTestUtils.runMapReduceJob(new ForestFireModelJobLauncher(), true, parameters, testFolders);
		return parseOutput(true);
	}

	@Override
	public GraphStructure executeUndirectedForestFireModel(GraphStructure graph, ForestFireModelParameters parameters)
			throws Exception {
		hadoopTestUtils.writeGraphToDirectory(graph, testFolders.getInputDirectory());
		hadoopTestUtils.runMapReduceJob(new ForestFireModelJobLauncher(), false, parameters, testFolders);
		return parseOutput(false);
	}

	private GraphStructure parseOutput(boolean outputIsDirected) throws IOException {
		final Pattern DIRECTED_LINE_PATTERN = Pattern.compile("([0-9]+)[ \t]*#[0-9,]*[ \t]+@([0-9,]*)[ \t]*$");
		final Pattern UNDIRECTED_LINE_PATTERN = Pattern.compile("([0-9]+)[ \t]*([0-9,]*)[ \t]*$");
		final Pattern COMMA = Pattern.compile(",");

		List<String> outputData = hadoopTestUtils.readOutputAsLines(testFolders);
		Map<Long, Set<Long>> edgeLists = new HashMap<>();
		for (String line : outputData) {
			Matcher lineMatcher = (outputIsDirected ? DIRECTED_LINE_PATTERN : UNDIRECTED_LINE_PATTERN).matcher(line);

			assertThat("each line of output matches the expected adjacency list output format",
					lineMatcher.matches(), is(true));

			long vertexId = Long.parseLong(lineMatcher.group(1));

			Set<Long> edgeList = new HashSet<>();
			String outEdgesString = lineMatcher.group(2);
			String[] outEdges = COMMA.split(outEdgesString);
			for (String outEdge : outEdges) {
				if (outEdge.length() != 0) {
					edgeList.add(Long.parseLong(outEdge));
				}
			}

			edgeLists.put(vertexId, edgeList);
		}
		return new GraphStructure(edgeLists);
	}

}
