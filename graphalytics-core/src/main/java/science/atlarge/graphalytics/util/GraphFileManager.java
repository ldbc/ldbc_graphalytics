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
package science.atlarge.graphalytics.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.duckdb.DuckDBConnection;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import science.atlarge.graphalytics.domain.graph.PropertyList;
import science.atlarge.graphalytics.util.io.VertexListInputStreamReader;
import science.atlarge.graphalytics.util.io.VertexListPropertyFilter;
import science.atlarge.graphalytics.util.io.VertexListStreamWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for managing graph files. Responsible for generating additional graph files from a source dataset
 * with subsets of properties.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public final class GraphFileManager {

	private static final Logger LOG = LogManager.getLogger();

	/**
	 * Prevent instantiation of utility class.
	 */
	private GraphFileManager() {
	}

	/**
	 * Checks if the vertex and edge files for a Graph exist, and tries to generate them if they do not.
	 *
	 * @param formattedGraph the graph to check the vertex and edge file for
	 * @throws IOException iff the vertex or edge file can not be generated
	 */
	public static void ensureGraphFilesExist(FormattedGraph formattedGraph) throws IOException, SQLException {
		ensureEdgeFileExists(formattedGraph);
	}

	private static void ensureEdgeFileExists(FormattedGraph formattedGraph) throws IOException, SQLException {
		if (Paths.get(formattedGraph.getEdgeFilePath()).toFile().exists()) {
			LOG.info("Found edge file for graph \"{}\" at \"{}\".", formattedGraph.getName(), formattedGraph.getEdgeFilePath());
			return;
		}

		FormattedGraph sourceGraph = formattedGraph.getGraph().getSourceGraph();
		if (!Paths.get(sourceGraph.getEdgeFilePath()).toFile().exists()) {
			throw new IOException("Source edge file is missing, can not generate graph files.");
		}

		LOG.info("Generating edge file for graph \"{}\" at \"{}\" with edge properties {}.",
				formattedGraph.getGraph().getName(), formattedGraph.getEdgeFilePath(), formattedGraph.getEdgeProperties());
		generateEdgeFile(formattedGraph);
		LOG.info("Done generating edge file for graph \"{}\".", formattedGraph.getGraph().getName());
	}

	private static void generateEdgeFile(FormattedGraph formattedGraph) throws IOException, SQLException {
		// Ensure that the output directory exists
		Files.createDirectories(Paths.get(formattedGraph.getEdgeFilePath()).getParent());

		String dbFile = String.format("%s/edge_file.duckdb", Paths.get(formattedGraph.getEdgeFilePath()).toFile().getParent());
		new File(dbFile).delete();

		try (DuckDBConnection conn = (DuckDBConnection) DriverManager.getConnection(
				String.format("jdbc:duckdb:%s", dbFile)
			)) {
			Statement stmt = conn.createStatement();
			stmt.execute("SET experimental_parallel_csv=true;");
			stmt.execute("CREATE OR REPLACE TABLE e(source BIGINT NOT NULL, target BIGINT NOT NULL, weight DOUBLE);");
			stmt.execute(String.format("COPY e FROM '%s' (DELIMITER ' ', FORMAT csv)", formattedGraph.getGraph().getSourceGraph().getEdgeFilePath()));
			// Drop a lot of weight with this one weird trick
			stmt.execute(String.format("COPY e (source, target) TO '%s' (DELIMITER ' ', FORMAT csv)", formattedGraph.getEdgeFilePath()));
		}
	}

}
