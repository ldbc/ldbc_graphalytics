/*
 * Copyright 2015 - 2017 Atlarge Research Team,
 * operating at Technische Universiteit Delft
 * and Vrije Universiteit Amsterdam, the Netherlands.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package science.atlarge.graphalytics.util.graph;

import science.atlarge.graphalytics.util.io.EdgeListInputStreamReader;
import science.atlarge.graphalytics.util.io.EdgeListStream;
import science.atlarge.graphalytics.util.io.VertexListInputStreamReader;
import science.atlarge.graphalytics.util.io.VertexListStream;

import java.io.*;

/**
 * Utility class for parsing graphs for the purpose of testing algorithm implementations.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public final class PropertyGraphParser<V, E> implements AutoCloseable {

	private final VertexListStream vertexList;
	private final EdgeListStream edgeList;
	private final boolean isDirected;
	private final PropertyGraph<V, E> graph;

	private PropertyGraphParser(VertexListStream vertexList, EdgeListStream edgeList, boolean isDirected) {
		this.vertexList = vertexList;
		this.edgeList = edgeList;
		this.isDirected = isDirected;
		this.graph = new PropertyGraph<>();
	}

	private void parseVertices(ValueParser<V> valueParser) throws IOException {
		while (vertexList.hasNextVertex()) {
			VertexListStream.VertexData vertex = vertexList.getNextVertex();
			graph.createVertex(vertex.getId(), valueParser.parse(vertex.getValues()));
		}
	}

	private void parseEdges(ValueParser<E> valueParser) throws IOException {
		while (edgeList.hasNextEdge()) {
			EdgeListStream.EdgeData edge = edgeList.getNextEdge();

			long sourceId = edge.getSourceId();
			long destinationId = edge.getDestinationId();
			E value = valueParser.parse(edge.getValues());

			graph.createEdge(sourceId, destinationId, value);
			if (!isDirected) {
				graph.createEdge(destinationId, sourceId, value);
			}
		}
	}

	private PropertyGraph<V, E> getGraph() {
		return graph;
	}

	@Override
	public void close() throws Exception {
		vertexList.close();
		edgeList.close();
	}

	/**
	 * Parses a vertex list file and edge list file into a PropertyGraph.
	 *
	 * @param vertexListPath    file path pointing to the vertex list file
	 * @param edgeListPath      file path pointing to the edge list file
	 * @param isDirected        true iff the input graph is directed
	 * @param vertexValueParser a custom parser for converting vertex properties to type V
	 * @param edgeValueParser   a custom parser for converting edge properties to type E
	 * @param <V>               the PropertyGraph vertex value type
	 * @param <E>               the PropertyGraph edge value type
	 * @return the parsed PropertyGraph
	 * @throws IOException iff an error occurred while parsing the input
	 */
	public static <V, E> PropertyGraph<V, E> parsePropertyGraph(String vertexListPath, String edgeListPath,
			boolean isDirected, ValueParser<V> vertexValueParser, ValueParser<E> edgeValueParser)
			throws IOException {
		return parsePropertyGraph(new VertexListInputStreamReader(new FileInputStream(vertexListPath)),
				new EdgeListInputStreamReader(new FileInputStream(edgeListPath)),
				isDirected, vertexValueParser, edgeValueParser);
	}

	/**
	 * Parses a vertex list stream and edge list stream into a PropertyGraph.
	 *
	 * @param vertexList        input vertex list stream
	 * @param edgeList          input edge list stream
	 * @param isDirected        true iff the input graph is directed
	 * @param vertexValueParser a custom parser for converting vertex properties to type V
	 * @param edgeValueParser   a custom parser for converting edge properties to type E
	 * @param <V>               the PropertyGraph vertex value type
	 * @param <E>               the PropertyGraph edge value type
	 * @return the parsed PropertyGraph
	 * @throws IOException iff an error occurred while parsing the input
	 */
	public static <V, E> PropertyGraph<V, E> parsePropertyGraph(VertexListStream vertexList, EdgeListStream edgeList,
			boolean isDirected, ValueParser<V> vertexValueParser, ValueParser<E> edgeValueParser)
			throws IOException {
		PropertyGraphParser<V, E> parser = new PropertyGraphParser<>(vertexList, edgeList, isDirected);
		parser.parseVertices(vertexValueParser);
		parser.parseEdges(edgeValueParser);
		return parser.getGraph();
	}

	/**
	 * Parser for converting an array of strings (properties for a single vertex or edge) into a single value type.
	 *
	 * @param <T> the type of value returned by this parser
	 */
	public interface ValueParser<T> {

		T parse(String[] valueTokens) throws IOException;

	}

}
