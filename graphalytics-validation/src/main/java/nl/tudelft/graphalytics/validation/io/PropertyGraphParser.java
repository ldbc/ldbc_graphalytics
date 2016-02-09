package nl.tudelft.graphalytics.validation.io;

import nl.tudelft.graphalytics.validation.PropertyGraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Utility class for parsing graphs for the purpose of testing algorithm implementations.
 *
 * @author Tim Hegeman
 */
public class PropertyGraphParser<V, E> {

	private final InputStream vertexList;
	private final InputStream edgeList;
	private final boolean isDirected;
	private final PropertyGraph<V, E> graph;

	private PropertyGraphParser(InputStream vertexList, InputStream edgeList, boolean isDirected) {
		this.vertexList = vertexList;
		this.edgeList = edgeList;
		this.isDirected = isDirected;
		this.graph = new PropertyGraph<>();
	}

	private void parseVertices(ValueParser<V> valueParser) throws IOException {
		try (BufferedReader datasetReader = new BufferedReader(new InputStreamReader(vertexList))) {
			for (String line = datasetReader.readLine(); line != null; line = datasetReader.readLine()) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}

				String tokens[] = line.split(" ");
				long id = Long.parseLong(tokens[0]);
				V value = valueParser.parse(Arrays.copyOfRange(tokens, 1, tokens.length));

				graph.createVertex(id, value);
			}
		}
	}

	private void parseEdges(ValueParser<E> valueParser) throws IOException {
		try (BufferedReader datasetReader = new BufferedReader(new InputStreamReader(edgeList))) {
			for (String line = datasetReader.readLine(); line != null; line = datasetReader.readLine()) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}

				String tokens[] = line.split(" ");
				long sourceId = Long.parseLong(tokens[0]);
				long destinationId = Long.parseLong(tokens[1]);
				E value = valueParser.parse(Arrays.copyOfRange(tokens, 2, tokens.length));

				graph.createEdge(sourceId, destinationId, value);
				if (!isDirected) {
					graph.createEdge(destinationId, sourceId, value);
				}
			}
		}
	}

	private PropertyGraph<V, E> getGraph() {
		return graph;
	}

	public static <V, E> PropertyGraph<V, E> parserPropertyGraph(InputStream vertexList, InputStream edgeList,
			boolean isDirected, ValueParser<V> vertexValueParser, ValueParser<E> edgeValueParser)
			throws IOException {
		PropertyGraphParser<V, E> parser = new PropertyGraphParser<>(vertexList, edgeList, isDirected);
		parser.parseVertices(vertexValueParser);
		parser.parseEdges(edgeValueParser);
		return parser.getGraph();
	}

	public interface ValueParser<T> {

		T parse(String[] valueTokens) throws IOException;

	}

}
