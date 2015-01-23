package nl.tudelft.graphalytics.neo4j;

import nl.tudelft.graphalytics.Platform;
import nl.tudelft.graphalytics.PlatformExecutionException;
import nl.tudelft.graphalytics.domain.*;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

import static nl.tudelft.graphalytics.neo4j.Neo4jConfiguration.EDGE;

/**
 * Entry point of the Graphalytics benchmark for Neo4j. Provides the platform
 * API required by the Graphalytics core to perform operations such as uploading
 * graphs and executing specific algorithms on specific graphs.
 *
 * @author Tim Hegeman
 */
public class Neo4jPlatform implements Platform {
	// TODO: Make configurable
	public static final String DB_PATH = "neo4j-data";

	@Override
	public void uploadGraph(Graph graph, String graphFilePath) throws Exception {
		BatchInserter inserter = BatchInserters.inserter(Paths.get(DB_PATH, graph.getName()).toString());
		try (BufferedReader graphData = new BufferedReader(new FileReader(graphFilePath))) {
			GraphFormat gf = graph.getGraphFormat();

			if (gf.isEdgeBased()) {
				parseEdgeBasedGraph(graphData, inserter, gf.isDirected());
			} else {
				parseVertexBasedGraph(graphData, inserter);
			}
		} finally {
			inserter.shutdown();
		}
	}

	private void parseEdgeBasedGraph(BufferedReader graphData, BatchInserter inserter, boolean isDirected) throws IOException {
		final Map<String, Object> EMPTY = Collections.emptyMap();

		String line;
		while ((line = graphData.readLine()) != null) {
			Scanner lineTokens = new Scanner(line);
			// Skip empty lines
			if (!lineTokens.hasNext())
				continue;

			// Read source and destination ID
			long sourceId = lineTokens.nextLong();
			long destinationId = lineTokens.nextLong();
			// Perform a sanity check
			if (lineTokens.hasNext())
				throw new InputMismatchException("Expected two node IDs, found \"" + line + "\".");

			// Insert the nodes if needed
			if (!inserter.nodeExists(sourceId))
				inserter.createNode(sourceId, EMPTY);
			if (!inserter.nodeExists(destinationId))
				inserter.createNode(destinationId, EMPTY);

			// Create the edge
			inserter.createRelationship(sourceId, destinationId, EDGE, EMPTY);
			if (!isDirected)
				inserter.createRelationship(destinationId, sourceId, EDGE, EMPTY);
		}
	}

	private void parseVertexBasedGraph(BufferedReader graphData, BatchInserter inserter) throws IOException {
		final Map<String, Object> EMPTY = Collections.emptyMap();

		String line;
		while ((line = graphData.readLine()) != null) {
			Scanner lineTokens = new Scanner(line);
			// Skip empty lines
			if (!lineTokens.hasNext())
				continue;

			// Read the source vertex and create a node if needed
			long sourceId = lineTokens.nextLong();
			if (!inserter.nodeExists(sourceId))
				inserter.createNode(sourceId, EMPTY);

			// Read any number of destination IDs
			while (lineTokens.hasNext()) {
				long destinationId = lineTokens.nextLong();

				// Insert the node if needed
				if (!inserter.nodeExists(destinationId))
					inserter.createNode(destinationId, EMPTY);

				// Create the edge
				inserter.createRelationship(sourceId, destinationId, EDGE, EMPTY);
			}
		}
	}

	@Override
	public PlatformBenchmarkResult executeAlgorithmOnGraph(Algorithm algorithm, Graph graph, Object parameters) throws PlatformExecutionException {
		return null;
	}

	@Override
	public void deleteGraph(String graphName) {
		// TODO
	}

	@Override
	public String getName() {
		return "neo4j";
	}

	@Override
	public PlatformConfiguration getPlatformConfiguration() {
		return null;
	}
}
