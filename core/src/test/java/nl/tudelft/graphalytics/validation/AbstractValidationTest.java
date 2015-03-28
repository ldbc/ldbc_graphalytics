package nl.tudelft.graphalytics.validation;

import nl.tudelft.graphalytics.validation.io.GraphValueParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for executing validation tests of implementations of the Graphalytics benchmark suite.
 *
 * @author Tim Hegeman
 */
public abstract class AbstractValidationTest {

	public static GraphStructure parseDirectedGraphStructureFromVertexBasedDataset(InputStream dataset)
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
					edges.get(sourceVertex).add(destinationVertex);
					if (!edges.containsKey(destinationVertex)) {
						edges.put(destinationVertex, new HashSet<Long>());
					}
				}
			}
			return new GraphStructure(edges);
		}
	}

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
