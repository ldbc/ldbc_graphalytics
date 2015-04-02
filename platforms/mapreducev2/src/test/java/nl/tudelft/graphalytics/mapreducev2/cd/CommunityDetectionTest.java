package nl.tudelft.graphalytics.mapreducev2.cd;

import nl.tudelft.graphalytics.domain.algorithms.CommunityDetectionParameters;
import nl.tudelft.graphalytics.mapreducev2.HadoopTestFolders;
import nl.tudelft.graphalytics.mapreducev2.HadoopTestUtils;
import nl.tudelft.graphalytics.validation.GraphStructure;
import nl.tudelft.graphalytics.validation.cd.CommunityDetectionOutput;
import nl.tudelft.graphalytics.validation.cd.CommunityDetectionValidationTest;
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
public class CommunityDetectionTest extends CommunityDetectionValidationTest {

	private static HadoopTestUtils hadoopTestUtils;

	@Rule
	public HadoopTestFolders testFolders = new HadoopTestFolders();

	@BeforeClass
	public static void setUp() throws IOException {
		hadoopTestUtils = new HadoopTestUtils();
		hadoopTestUtils.startCluster(CommunityDetectionTest.class.getName());
	}

	@AfterClass
	public static void tearDown() {
		hadoopTestUtils.shutdownCluster();
	}

	@Override
	public CommunityDetectionOutput executeDirectedCommunityDetection(GraphStructure graph,
			CommunityDetectionParameters parameters) throws Exception {
		hadoopTestUtils.writeGraphToDirectory(graph, testFolders.getRawInputDirectory());
		hadoopTestUtils.convertGraphToHadoopFormat(testFolders.getRawInputDirectory(), testFolders.getInputDirectory());
		hadoopTestUtils.runMapReduceJob(new CommunityDetectionJobLauncher(), true, parameters, testFolders);
		return parseOutput();
	}

	@Override
	public CommunityDetectionOutput executeUndirectedCommunityDetection(GraphStructure graph,
			CommunityDetectionParameters parameters) throws Exception {
		hadoopTestUtils.writeGraphToDirectory(graph, testFolders.getInputDirectory());
		hadoopTestUtils.runMapReduceJob(new CommunityDetectionJobLauncher(), false, parameters, testFolders);
		return parseOutput();
	}

	private CommunityDetectionOutput parseOutput() throws IOException {
		final Pattern LINE_PATTERN = Pattern.compile("([0-9]+)[ \t].*\\$([0-9]+)\\|.*");

		List<String> outputData = hadoopTestUtils.readOutputAsLines(testFolders);
		Map<Long, Long> communityIds = new HashMap<>();
		for (String line : outputData) {
			Matcher lineMatcher = LINE_PATTERN.matcher(line);

			assertThat("each line of output matches the expected adjacency list output format",
					lineMatcher.matches(), is(true));

			long vertexId = Long.parseLong(lineMatcher.group(1));
			long communityId = Long.parseLong(lineMatcher.group(2));

			communityIds.put(vertexId, communityId);
		}

		return new CommunityDetectionOutput(communityIds);
	}

}
