package nl.tudelft.graphalytics.giraph;

import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.utils.InternalVertexRunner;
import org.apache.giraph.utils.TestGraph;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Writable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class for Giraph algorithm tests. This class handles graph loading and parsing, using generics to allow for
 * different value types for both vertices and edges.
 *
 * @author Tim Hegeman
 */
public abstract class AbstractComputationTest<V extends Writable, E extends Writable> {

	/**
	 * Parses an example resource containing a directed graph in vertex-based format, and performs the computation
	 * specified in the Giraph configuration in-memory. The result of this computation is returned in a TestGraph.
	 *
	 * @param giraphConfiguration job configuration for the test, must have at least a Computation set
	 * @param graphResource       the name of the graph resource on the classpath
	 * @return the result of the in-memory execution of the specified graph computation
	 * @throws Exception if an exception occurs during parsing or computation execution
	 */
	protected TestGraph<LongWritable, V, E> runTest(GiraphConfiguration giraphConfiguration,
	                                                String graphResource) throws Exception {
		TestGraph<LongWritable, V, E> inputGraph = parseGraphStructure(giraphConfiguration, graphResource);
		return InternalVertexRunner.runWithInMemoryOutput(giraphConfiguration, inputGraph);
	}

	/**
	 * Parses a directed vertex-based graph from a file.
	 *
	 * @param giraphConfiguration job configuration for the test
	 * @param resource            the name of the graph resource on the classpath
	 * @return the parsed graph
	 * @throws IOException if an error occurs while reading the file
	 */
	protected TestGraph<LongWritable, V, E>
	parseGraphStructure(GiraphConfiguration giraphConfiguration, String resource) throws IOException {
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
				AbstractComputationTest.class.getResourceAsStream(resource)))) {
			Set<Long> vertices = new HashSet<>();
			Map<Long, Set<Long>> edges = new HashMap<>();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] tokens = line.split(" ");
				long sourceId = Long.parseLong(tokens[0]);
				vertices.add(sourceId);
				edges.put(sourceId, new HashSet<Long>());
				for (int i = 1; i < tokens.length; i++) {
					long destinationId = Long.parseLong(tokens[i]);
					vertices.add(destinationId);
					edges.get(sourceId).add(destinationId);
				}
			}

			TestGraph<LongWritable, V, E> parsedGraph = new TestGraph<>(giraphConfiguration);
			for (long vertexId : vertices)
				parsedGraph.addVertex(new LongWritable(vertexId), getDefaultValue(vertexId));
			for (long sourceId : edges.keySet())
				for (long destinationId : edges.get(sourceId))
					parsedGraph.addEdge(new LongWritable(sourceId), new LongWritable(destinationId),
							getDefaultEdgeValue(sourceId, destinationId));
			return parsedGraph;
		}
	}

	/**
	 * Parses a graph from a file containing per line: "[vertex] [value]". The value is parsed using the
	 * {@link #parseValue(long, String)} method that must be implemented by the concrete test class.
	 *
	 * @param giraphConfiguration job configuration for the test
	 * @param resource            the name of the graph resource on the classpath
	 * @return the parsed graph
	 * @throws IOException if an error occurs while reading the file
	 */
	protected TestGraph<LongWritable, V, E>
	parseGraphValues(GiraphConfiguration giraphConfiguration, String resource) throws IOException {
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
				AbstractComputationTest.class.getResourceAsStream(resource)))) {
			return parseGraphValues(giraphConfiguration, bufferedReader);
		}
	}

	/**
	 * Parses a graph from a file containing per line: "[vertex] [value]". The value is parsed using the
	 * {@link #parseValue(long, String)} method that must be implemented by the concrete test class.
	 *
	 * @param giraphConfiguration job configuration for the test
	 * @param bufferedReader      a reader for the input data
	 * @return the parsed graph
	 * @throws IOException if an error occurs while reading the file
	 */
	protected TestGraph<LongWritable, V, E>
	parseGraphValues(GiraphConfiguration giraphConfiguration, BufferedReader bufferedReader) throws IOException {
		TestGraph<LongWritable, V, E> parsedGraph = new TestGraph<>(giraphConfiguration);
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			String[] tokens = line.split(" ", 2);
			long vertexId = Long.parseLong(tokens[0]);
			V value = parseValue(vertexId, tokens[1]);
			parsedGraph.addVertex(new LongWritable(vertexId), value);
		}
		return parsedGraph;
	}

	/**
	 * @param vertexId the id of a vertex in a parsed graph
	 * @return the initial value for this vertex
	 */
	protected abstract V getDefaultValue(long vertexId);

	/**
	 * @param sourceId      the source id of an edge in a parsed graph
	 * @param destinationId the destination id of an edge in a parsed graph
	 * @return the initial value for this edge
	 */
	protected abstract E getDefaultEdgeValue(long sourceId, long destinationId);

	/**
	 * @param vertexId the id of a vertex in a parsed graph
	 * @param value    a String representing the value of this vertex
	 * @return the parsed value of this vertex
	 */
	protected abstract V parseValue(long vertexId, String value);

}
