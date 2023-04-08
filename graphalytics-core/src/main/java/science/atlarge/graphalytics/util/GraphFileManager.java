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
import science.atlarge.graphalytics.domain.graph.FormattedGraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
	public static void ensureGraphFilesExist(FormattedGraph formattedGraph) throws IOException, InterruptedException {
		ensureEdgeFileExists(formattedGraph);
	}

	private static void ensureEdgeFileExists(FormattedGraph formattedGraph) throws IOException, InterruptedException {
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

	private static void generateEdgeFile(FormattedGraph formattedGraph) throws IOException, InterruptedException {
		// Ensure that the output directory exists
		Files.createDirectories(Paths.get(formattedGraph.getEdgeFilePath()).getParent());

		List<String> command = new ArrayList<>();
		command.add("/bin/bash");
		command.add("-c");
		command.add(String.format("cut -d' ' -f1,2 %s > %s",
				formattedGraph.getGraph().getSourceGraph().getEdgeFilePath(),
				formattedGraph.getEdgeFilePath()
		));

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		int returnCode = process.waitFor();
		if (returnCode != 0) {
			throw new IOException("Creating minimized edge file failed");
		}
	}

}
