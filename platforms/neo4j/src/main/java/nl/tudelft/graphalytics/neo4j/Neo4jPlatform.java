/**
 * Copyright 2015 Delft University of Technology
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
package nl.tudelft.graphalytics.neo4j;

import nl.tudelft.graphalytics.Platform;
import nl.tudelft.graphalytics.PlatformExecutionException;
import nl.tudelft.graphalytics.domain.*;
import nl.tudelft.graphalytics.neo4j.bfs.BreadthFirstSearchJob;
import nl.tudelft.graphalytics.neo4j.cd.CommunityDetectionJob;
import nl.tudelft.graphalytics.neo4j.conn.ConnectedComponentsJob;
import nl.tudelft.graphalytics.neo4j.evo.ForestFireModelJob;
import nl.tudelft.graphalytics.neo4j.stats.LocalClusteringCoefficientJob;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static nl.tudelft.graphalytics.neo4j.Neo4jConfiguration.ID_PROPERTY;
import static nl.tudelft.graphalytics.neo4j.Neo4jConfiguration.VertexLabelEnum.Vertex;

/**
 * Entry point of the Graphalytics benchmark for Neo4j. Provides the platform
 * API required by the Graphalytics core to perform operations such as uploading
 * graphs and executing specific algorithms on specific graphs.
 *
 * @author Tim Hegeman
 */
public class Neo4jPlatform implements Platform {

    private static final Logger LOG = LogManager.getLogger();

    /**
     * Property key for the directory in which to store Neo4j databases.
     */
    public static final String DB_PATH_KEY = "neo4j.db.path";
    /**
     * Default value for the directory in which to store Neo4j databases.
     */
    public static final String DB_PATH = "neo4j-data";

    private static final String PROPERTIES_FILENAME = "neo4j.properties";
    public static final String PROPERTIES_PATH = "/" + PROPERTIES_FILENAME;

    private static final int INDEX_WAIT_TIMEOUT_SECONDS = 10;
    private static final int TRANSACTION_SIZE = 1000;

    private String dbPath;

    public Neo4jPlatform() {
        loadConfiguration();
    }

    private void loadConfiguration() {
        // Load Neo4j-specific configuration
        Configuration neo4jConfig;
        try {
            neo4jConfig = new PropertiesConfiguration(PROPERTIES_FILENAME);
        } catch (ConfigurationException e) {
            // Fall-back to an empty properties file
            LOG.info(format("Could not find or load %s", PROPERTIES_FILENAME));
            neo4jConfig = new PropertiesConfiguration();
        }
        dbPath = neo4jConfig.getString(DB_PATH_KEY, DB_PATH);
    }

    // TODO rewrite this
    @Override
    public void uploadGraph(Graph graph, String graphFilePath) throws Exception {
        URL properties = getClass().getResource(PROPERTIES_PATH);
        String databasePath = Paths.get(dbPath, graph.getName()).toString();
        try (Neo4jDatabase db = new Neo4jDatabase(databasePath, properties);
             BufferedReader graphData = new BufferedReader(new FileReader(graphFilePath));
             Neo4jDatabaseImporter importer = new Neo4jDatabaseImporter(db.get(), TRANSACTION_SIZE)) {
            createIndexOnVertexId(db.get());

            GraphFormat gf = graph.getGraphFormat();
            if (gf.isEdgeBased()) {
                parseEdgeBasedGraph(graphData, importer, gf.isDirected());
            } else {
                parseVertexBasedGraph(graphData, importer);
            }
        }
    }

    private static void createIndexOnVertexId(GraphDatabaseService db) {
        IndexDefinition indexDefinition;
        try (Transaction tx = db.beginTx()) {
            indexDefinition = db.schema().indexFor(Vertex).on(ID_PROPERTY).create();
            tx.success();
        }

        boolean indexIsOnline = false;
        while (!indexIsOnline) {
            try (Transaction tx = db.beginTx()) {
                db.schema().awaitIndexOnline(indexDefinition, INDEX_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                indexIsOnline = true;
                tx.success();
            } catch (IllegalStateException e) {
                if (db.schema().getIndexState(indexDefinition) == Schema.IndexState.FAILED) {
                    throw e;
                }
            }
        }
    }

    private static void parseEdgeBasedGraph(BufferedReader graphData, Neo4jDatabaseImporter importer,
                                            boolean isDirected) throws IOException {
        String line;
        while ((line = graphData.readLine()) != null) {
            Scanner lineTokens = new Scanner(line);
            // Skip empty lines
            if (!lineTokens.hasNext()) {
                continue;
            }

            // Read source and destination ID
            long sourceId = lineTokens.nextLong();
            long destinationId = lineTokens.nextLong();
            // Perform a sanity check
            if (lineTokens.hasNext()) {
                throw new InputMismatchException("Expected two node IDs, found \"" + line + "\".");
            }

            // Create the edge
            importer.createEdge(sourceId, destinationId);
            if (!isDirected) {
                importer.createEdge(destinationId, sourceId);
            }
        }
    }

    private void parseVertexBasedGraph(BufferedReader graphData, Neo4jDatabaseImporter importer) throws IOException {
        String line;
        while ((line = graphData.readLine()) != null) {
            Scanner lineTokens = new Scanner(line);
            // Skip empty lines
            if (!lineTokens.hasNext()) {
                continue;
            }

            // Read the source vertex
            long sourceId = lineTokens.nextLong();

            // Read any number of destination IDs
            while (lineTokens.hasNext()) {
                long destinationId = lineTokens.nextLong();

                // Create the edge
                importer.createEdge(sourceId, destinationId);
            }
        }
    }

    @Override
    public PlatformBenchmarkResult executeAlgorithmOnGraph(Algorithm algorithm, Graph graph, Object parameters)
            throws PlatformExecutionException {
        // Create a copy of the database that is used to store the algorithm results
        String graphDbPath = Paths.get(dbPath, graph.getName()).toString();
        String graphDbCopyPath = Paths.get(dbPath, graph.getName() + "-" + algorithm).toString();
        copyDatabase(graphDbPath, graphDbCopyPath);

        // Execute the algorithm
        try {
            Neo4jJob job = createJob(graphDbCopyPath, algorithm, parameters);
            job.run();
        } finally {
            // Clean up the database copy
            deleteDatabase(graphDbCopyPath);
        }

        return new PlatformBenchmarkResult(NestedConfiguration.empty());
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
            deleteDatabase(Paths.get(dbPath, graphName).toString());
        } catch (PlatformExecutionException e) {
            LOG.error("Failed to clean up the graph database at " + Paths.get(dbPath, graphName).toString() + ".", e);
        }
    }

    @Override
    public String getName() {
        return "neo4j";
    }

    @Override
    public NestedConfiguration getPlatformConfiguration() {
        try {
            Configuration configuration = new PropertiesConfiguration(PROPERTIES_FILENAME);
            return NestedConfiguration.fromExternalConfiguration(configuration, PROPERTIES_FILENAME);
        } catch (ConfigurationException ex) {
            return NestedConfiguration.empty();
        }
    }

}
