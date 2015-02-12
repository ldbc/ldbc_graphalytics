package nl.tudelft.graphalytics.neo4j;

import nl.tudelft.graphalytics.Platform;
import nl.tudelft.graphalytics.PlatformExecutionException;
import nl.tudelft.graphalytics.domain.*;
import nl.tudelft.graphalytics.neo4j.bfs.BreadthFirstSearchJob;
import nl.tudelft.graphalytics.neo4j.cd.CommunityDetectionJob;
import nl.tudelft.graphalytics.neo4j.conn.ConnectedComponentsJob;
import nl.tudelft.graphalytics.neo4j.evo.ForestFireModelJob;
import nl.tudelft.graphalytics.neo4j.stats.LocalClusteringCoefficientJob;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static nl.tudelft.graphalytics.neo4j.Neo4jConfiguration.EDGE;

/**
 * Entry point of the Graphalytics benchmark for Neo4j. Provides the platform
 * API required by the Graphalytics core to perform operations such as uploading
 * graphs and executing specific algorithms on specific graphs.
 *
 * @author Tim Hegeman
 */
public class Neo4jPlatform implements Platform {

	private static final Logger LOG = LogManager.getLogger();

	// TODO: Make configurable
	public static final String DB_PATH = "neo4j-data";
	public static final String PROPERTIES_PATH = "/neo4j.properties";

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

	private Map<String, Object> createPropertyMap(long vertexId) {
		Map<String, Object> propertyMap = new HashMap<>();
		propertyMap.put(Neo4jConfiguration.ID_PROPERTY, vertexId);
		return propertyMap;
	}

	private void parseEdgeBasedGraph(BufferedReader graphData, BatchInserter inserter, boolean isDirected)
			throws IOException {
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
				inserter.createNode(sourceId, createPropertyMap(sourceId));
			if (!inserter.nodeExists(destinationId))
				inserter.createNode(destinationId, createPropertyMap(destinationId));

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
				inserter.createNode(sourceId, createPropertyMap(sourceId));

			// Read any number of destination IDs
			while (lineTokens.hasNext()) {
				long destinationId = lineTokens.nextLong();

				// Insert the node if needed
				if (!inserter.nodeExists(destinationId))
					inserter.createNode(destinationId, createPropertyMap(destinationId));

				// Create the edge
				inserter.createRelationship(sourceId, destinationId, EDGE, EMPTY);
			}
		}
	}

	@Override
	public PlatformBenchmarkResult executeAlgorithmOnGraph(Algorithm algorithm, Graph graph, Object parameters)
			throws PlatformExecutionException {
		// Create a copy of the database that is used to store the algorithm results
		String dbPath = Paths.get(DB_PATH, graph.getName()).toString();
		String dbCopyPath = Paths.get(DB_PATH, graph.getName() + "-" + algorithm).toString();
		copyDatabase(dbPath, dbCopyPath);

		// Execute the algorithm
		try {
			Neo4jJob job = createJob(dbCopyPath, algorithm, parameters);
			job.run();
		} finally {
			// Clean up the database copy
			deleteDatabase(dbCopyPath);
		}

		return new PlatformBenchmarkResult(PlatformConfiguration.empty());
	}

	private Neo4jJob createJob(String databasePath, Algorithm algorithm, Object parameters)
			throws PlatformExecutionException {
		URL properties = getClass().getResource(PROPERTIES_PATH);
		switch (algorithm) {
			case BFS:
				return new BreadthFirstSearchJob(databasePath, properties, parameters);
			case CD:
				return new CommunityDetectionJob(databasePath, properties, parameters);
			case CONN:
				return new ConnectedComponentsJob(databasePath, properties);
			case EVO:
				return new ForestFireModelJob(databasePath, properties, parameters);
			case STATS:
				return new LocalClusteringCoefficientJob(databasePath, properties);
			default:
				throw new PlatformExecutionException("Algorithm not supported: " + algorithm);
		}
	}

	private void copyDatabase(String sourcePath, String destinationPath) throws PlatformExecutionException {
		try {
			FileUtils.copyDirectory(Paths.get(sourcePath).toFile(), Paths.get(destinationPath).toFile());
		} catch (IOException ex) {
			throw new PlatformExecutionException("Unable to create a temporary copy of the graph database", ex);
		}
	}

	private void deleteDatabase(String databasePath) throws PlatformExecutionException {
		try {
			FileUtils.deleteDirectory(Paths.get(databasePath).toFile());
		} catch (IOException e) {
			throw new PlatformExecutionException("Unable to clean up the graph database", e);
		}
	}

	@Override
	public void deleteGraph(String graphName) {
		try {
			deleteDatabase(Paths.get(DB_PATH, graphName).toString());
		} catch (PlatformExecutionException e) {
			LOG.error("Failed to clean up the graph database at " + Paths.get(DB_PATH, graphName).toString() + ".", e);
		}
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
