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
package nl.tudelft.graphalytics.mapreducev2;

import nl.tudelft.graphalytics.domain.Graph;
import nl.tudelft.graphalytics.domain.GraphFormat;
import nl.tudelft.graphalytics.mapreducev2.conversion.DirectedVertexToAdjacencyListConversion;
import nl.tudelft.graphalytics.validation.GraphStructure;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapreduce.v2.MiniMRYarnCluster;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for Hadoop algorithm tests that handles the setting up and tearing down of a Hadoop mini-cluster.
 *
 * @author Tim Hegeman
 */
public class HadoopTestUtils {

	private MiniMRYarnCluster mrCluster;

	public HadoopTestUtils() {
	}

	public void startCluster(String testName) throws IOException {
		mrCluster = new MiniMRYarnCluster(testName);
		mrCluster.init(new Configuration());
		mrCluster.start();
	}

	public void shutdownCluster() {
		mrCluster.stop();
	}

	public Configuration createConfiguration() {
		return new JobConf(mrCluster.getConfig());
	}

	public void writeGraphToDirectory(GraphStructure graph, File outputDirectory) throws IOException {
		if (!outputDirectory.exists()) {
			Files.createDirectory(outputDirectory.toPath());
		}

		File outputFile = new File(outputDirectory, "graph-file");
		try (PrintWriter writer = new PrintWriter(outputFile)) {
			for (long vertexId : graph.getVertices()) {
				writer.print(vertexId);
				for (long destinationId : graph.getEdgesForVertex(vertexId)) {
					writer.print(" ");
					writer.print(destinationId);
				}
				writer.println();
			}
			if (writer.checkError()) {
				throw new IOException("Encountered error while using PrintWriter.");
			}
		}
	}

	public void convertGraphToHadoopFormat(File inputDirectory, File outputDirectory) throws InterruptedException,
			IOException, ClassNotFoundException {
		new DirectedVertexToAdjacencyListConversion(inputDirectory.getPath(), outputDirectory.getPath()).run();
	}

	public void runMapReduceJob(MapReduceJobLauncher jobLauncher, boolean graphIsDirected, Object algorithmParameters,
			HadoopTestFolders testFolders) throws Exception {
		jobLauncher.parseGraphData(new Graph("", "", new GraphFormat(graphIsDirected, false)), algorithmParameters);
		jobLauncher.setInputPath(testFolders.getInputDirectory().getPath());
		jobLauncher.setIntermediatePath(testFolders.getIntermediateDirectory().getPath());
		jobLauncher.setOutputPath(testFolders.getOutputDirectory().getPath());
		jobLauncher.run(new String[0]);
	}

	public List<String> readOutputAsLines(HadoopTestFolders testFolders) throws IOException {
		File outputDirectory = testFolders.getOutputDirectory();
		File[] outputFiles = outputDirectory.listFiles();
		if (outputFiles == null || outputFiles.length == 0) {
			return new ArrayList<>();
		}

		List<String> outputLines = new ArrayList<>();
		for (File outputFile : outputFiles) {
			if (outputFile.getName().startsWith("part-")) {
				outputLines.addAll(Files.readAllLines(outputFile.toPath(), Charset.defaultCharset()));
			}
		}
		return outputLines;
	}

}
