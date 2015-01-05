package org.tudelft.graphalytics.giraph.bfs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.io.formats.IdWithValueTextOutputFormat;
import org.apache.giraph.io.formats.LongLongNullTextInputFormat;
import org.apache.giraph.utils.InternalVertexRunner;
import org.junit.Test;
import org.tudelft.graphalytics.giraph.test.LongLongJobResultParser;
import org.tudelft.graphalytics.giraph.test.Pair;

/**
 * Test class for BFSComputation. Executes the BFS computation on a small graph,
 * and verifies that the output of the computation matches the expected results.
 *
 * @author Tim Hegeman
 */
public class BFSComputationTest {

	@Test
	public void testToyData() throws Exception {
		// Construct a graph to test with
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(getClass().getResourceAsStream("/bfs/input")));
		Collection<String> graph = new ArrayList<>();
		String line;
		while ((line = reader.readLine()) != null)
			graph.add(line);
		reader.close();
		
		// Prepare the job configuration
		GiraphConfiguration conf = new GiraphConfiguration();
		conf.setComputationClass(BFSComputation.class);
		conf.setVertexInputFormatClass(LongLongNullTextInputFormat.class);
		conf.setVertexOutputFormatClass(IdWithValueTextOutputFormat.class);
		BFSConfiguration.SOURCE_VERTEX.set(conf, 1);
		
		// Get the output of the BFS job
		Iterable<String> result = InternalVertexRunner.run(conf, graph.toArray(new String[graph.size()]));
		Collection<Pair<Long, Long>> parsed = parseJobResult(result);
		
		// Load the expected output
		Collection<Pair<Long, Long>> expected = LongLongJobResultParser.parse(
				getClass().getResource("/bfs/output").getPath());
		
		// Assert that the job output is equal to the expected output
		assertThat(parsed, hasSize(expected.size()));
		for (Pair<Long, Long> outcome : expected)
			assertThat(parsed, hasItem(equalTo(outcome)));
	}
	
	private Collection<Pair<Long, Long>> parseJobResult(Iterable<String> jobOutput) {
		Collection<Pair<Long, Long>> parsed = new ArrayList<>();
		Pattern SEPARATOR = Pattern.compile("[\t ]");
		// Loop through the lines of job output
		for (String line : jobOutput) {
			// Parse the vertex ID + value and add it to the list
			String[] tokens = SEPARATOR.split(line);
			parsed.add(new Pair<>(Long.parseLong(tokens[0]), Long.parseLong(tokens[1])));
		}
		return parsed;
	}
	
}
