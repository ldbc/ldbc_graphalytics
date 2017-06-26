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
package science.atlarge.graphalytics.validation.io;

import science.atlarge.graphalytics.validation.GraphStructure;
import science.atlarge.graphalytics.validation.GraphValues;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for parsing graphs for the purpose of testing algorithm implementations.
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class GraphParser {

	/**
	 * Parses a graph structure (vertices and edges without values) from an input stream. The input is assumed to be in
	 * vertex-based format; each line of the input contains a vertex id followed by zero or more vertex ids, one for
	 * each outgoing edge, separated by spaces.
	 *
	 * @param dataset  the input stream containing the vertex-based dataset representation
	 * @param directed true iff the dataset is a directed graph
	 * @return the parsed graph
	 * @throws IOException iff the dataset could not be read
	 */
	public static GraphStructure parseGraphStructureFromVertexBasedDataset(InputStream dataset, boolean directed)
			throws IOException {
		try (BufferedReader datasetReader = new BufferedReader(new InputStreamReader(dataset))) {
			Map<Long, Set<Long>> edges = new HashMap<>();
			for (String line = datasetReader.readLine(); line != null; line = datasetReader.readLine()) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}

				String tokens[] = line.split(" ");
				long sourceVertex = Long.parseLong(tokens[0]);
				edges.put(sourceVertex, new HashSet<Long>());

				for (int i = 1; i < tokens.length; i++) {
					long destinationVertex = Long.parseLong(tokens[i]);
					if (!edges.containsKey(destinationVertex)) {
						edges.put(destinationVertex, new HashSet<Long>());
					}

					edges.get(sourceVertex).add(destinationVertex);
					if (!directed) {
						edges.get(destinationVertex).add(sourceVertex);
					}
				}
			}
			return new GraphStructure(edges);
		}
	}

	/**
	 * Parses a set of vertices and corresponding values from an input stream. The input format is a single line per
	 * vertex. Each line contains a vertex id followed by a space and the vertex value as a string. The string
	 * representations of the vertex values a parsed using a given parser.
	 *
	 * @param dataset     the input stream containing the dataset representation
	 * @param valueParser a parser for string representations of vertex values
	 * @return the parsed vertices and values
	 * @throws IOException iff the dataset could not be read
	 */
	public static <ValueType> GraphValues<ValueType> parseGraphValuesFromDataset(
			InputStream dataset, GraphValueParser<ValueType> valueParser) throws IOException {
		try (BufferedReader datasetReader = new BufferedReader(new InputStreamReader(dataset))) {
			Map<Long, ValueType> values = new HashMap<>();
			for (String line = datasetReader.readLine(); line != null; line = datasetReader.readLine()) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}

				String tokens[] = line.split(" ", 2);
				long vertexId = Long.parseLong(tokens[0]);
				ValueType vertexValue = valueParser.parseValue(tokens[1]);
				values.put(vertexId, vertexValue);
			}
			return new GraphValues<>(values);
		}
	}

}
