package nl.tudelft.graphalytics.util;

import nl.tudelft.graphalytics.domain.Graph;
import nl.tudelft.graphalytics.domain.graph.PropertyList;
import nl.tudelft.graphalytics.util.io.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Utility class for managing graph files. Responsible for generating additional graph files from a source dataset
 * with subsets of properties.
 *
 * @author Tim Hegeman
 */
public final class GraphFileManager {

	/**
	 * Prevent instantiation of utility class.
	 */
	private GraphFileManager() {
	}

	/**
	 * Checks if the vertex and edge files for a Graph exist, and tries to genereate them if they do not.
	 *
	 * @param graph the graph to check the vertex and edge file for
	 * @throws IOException iff the vertex or edge file can not be generated
	 */
	public static void ensureGraphFilesExist(Graph graph) throws IOException {
		ensureVertexFileExists(graph);
		ensureEdgeFileExists(graph);
	}

	private static void ensureVertexFileExists(Graph graph) throws IOException {
		if (Paths.get(graph.getVertexFilePath()).toFile().exists()) {
			return;
		}

		Graph sourceGraph = graph.getGraphSet().getSourceGraph();
		if (!Paths.get(sourceGraph.getVertexFilePath()).toFile().exists()) {
			throw new IOException("Source vertex file is missing, can not generate graph files.");
		}

		generateVertexFile(graph);
	}

	private static void ensureEdgeFileExists(Graph graph) throws IOException {
		if (Paths.get(graph.getEdgeFilePath()).toFile().exists()) {
			return;
		}

		Graph sourceGraph = graph.getGraphSet().getSourceGraph();
		if (!Paths.get(sourceGraph.getEdgeFilePath()).toFile().exists()) {
			throw new IOException("Source edge file is missing, can not generate graph files.");
		}

		generateEdgeFile(graph);
	}

	private static void generateVertexFile(Graph graph) throws IOException {
		int[] propertyIndices = findPropertyIndices(graph.getGraphSet().getSourceGraph().getVertexProperties(),
				graph.getVertexProperties());
		try (VertexListStreamWriter writer = new VertexListStreamWriter(
				new VertexListPropertyFilter(
						new VertexListInputStreamReader(
								new FileInputStream(graph.getGraphSet().getSourceGraph().getVertexFilePath())
						),
						propertyIndices),
				new FileOutputStream(graph.getVertexFilePath()))) {
			writer.writeAll();
		}
	}

	private static void generateEdgeFile(Graph graph) throws IOException {
		int[] propertyIndices = findPropertyIndices(graph.getGraphSet().getSourceGraph().getEdgeProperties(),
				graph.getEdgeProperties());
		try (EdgeListStreamWriter writer = new EdgeListStreamWriter(
				new EdgeListPropertyFilter(
						new EdgeListInputStreamReader(
								new FileInputStream(graph.getGraphSet().getSourceGraph().getEdgeFilePath())
						),
						propertyIndices),
				new FileOutputStream(graph.getEdgeFilePath()))) {
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
