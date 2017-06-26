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

import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import science.atlarge.graphalytics.domain.graph.PropertyList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.util.io.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
	public static void ensureGraphFilesExist(FormattedGraph formattedGraph) throws IOException {
		ensureVertexFileExists(formattedGraph);
		ensureEdgeFileExists(formattedGraph);
	}

	private static void ensureVertexFileExists(FormattedGraph formattedGraph) throws IOException {
		if (Paths.get(formattedGraph.getVertexFilePath()).toFile().exists()) {
			LOG.info("Found vertex file for graph \"{}\" at \"{}\".", formattedGraph.getGraph().getName(), formattedGraph.getVertexFilePath());
			return;
		}

		FormattedGraph sourceGraph = formattedGraph.getGraph().getSourceGraph();
		if (!Paths.get(sourceGraph.getVertexFilePath()).toFile().exists()) {
			throw new IOException("Source vertex file is missing, can not generate graph files.");
		}

		LOG.info("Generating vertex file for graph \"{}\" at \"{}\" with vertex properties {}.",
				formattedGraph.getGraph().getName(), formattedGraph.getVertexFilePath(), formattedGraph.getVertexProperties());
		generateVertexFile(formattedGraph);
		LOG.info("Done generating vertex file for graph \"{}\".", formattedGraph.getGraph().getName());
	}

	private static void ensureEdgeFileExists(FormattedGraph formattedGraph) throws IOException {
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

	private static void generateVertexFile(FormattedGraph formattedGraph) throws IOException {
		// Ensure that the output directory exists
		Files.createDirectories(Paths.get(formattedGraph.getVertexFilePath()).getParent());

		// Generate the vertex file
		int[] propertyIndices = findPropertyIndices(formattedGraph.getGraph().getSourceGraph().getVertexProperties(),
				formattedGraph.getVertexProperties());
		try (VertexListStreamWriter writer = new VertexListStreamWriter(
				new VertexListPropertyFilter(
						new VertexListInputStreamReader(
								new FileInputStream(formattedGraph.getGraph().getSourceGraph().getVertexFilePath())
						),
						propertyIndices),
				new FileOutputStream(formattedGraph.getVertexFilePath()))) {
			writer.writeAll();
		}
	}

	private static void generateEdgeFile(FormattedGraph formattedGraph) throws IOException {
		// Ensure that the output directory exists
		Files.createDirectories(Paths.get(formattedGraph.getEdgeFilePath()).getParent());

		// Generate the edge file
		int[] propertyIndices = findPropertyIndices(formattedGraph.getGraph().getSourceGraph().getEdgeProperties(),
				formattedGraph.getEdgeProperties());
		try (EdgeListStreamWriter writer = new EdgeListStreamWriter(
				new EdgeListPropertyFilter(
						new EdgeListInputStreamReader(
								new FileInputStream(formattedGraph.getGraph().getSourceGraph().getEdgeFilePath())
						),
						propertyIndices),
				new FileOutputStream(formattedGraph.getEdgeFilePath()))) {
			writer.writeAll();
		}
	}

	private static int[] findPropertyIndices(PropertyList sourceList, PropertyList targetList) throws IOException {
		int[] propertyIndices = new int[targetList.size()];
		for (int i = 0; i < targetList.size(); i++) {
			propertyIndices[i] = sourceList.indexOf(targetList.get(i));
		}
		return propertyIndices;
	}

}
