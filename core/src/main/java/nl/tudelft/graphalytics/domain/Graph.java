package nl.tudelft.graphalytics.domain;

import java.io.Serializable;

/**
 * Represents a single graph in the Graphalytics benchmark suite. Each graph has
 * a unique name, a path to a file containing the graph data, and a format
 * specification.
 *
 * @author Tim Hegeman
 */
public class Graph implements Serializable {

	private final String name;
	private final String relativeFilePath;
	private final GraphFormat graphFormat;

	/**
	 * @param name             the unique name of the graph
	 * @param relativeFilePath the path of the graph file, relative to the
	 *                         graph root directory
	 * @param graphFormat      the format of the graph
	 */
	public Graph(String name, String relativeFilePath, GraphFormat graphFormat) {
		this.name = name;
		this.relativeFilePath = relativeFilePath;
		this.graphFormat = graphFormat;
	}

	/**
	 * @return the unique name of the graph
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the path of the graph file, relative to the graph root directory
	 */
	public String getRelativeFilePath() {
		return relativeFilePath;
	}

	/**
	 * @return the graph format specification
	 */
	public GraphFormat getFormat() {
		return graphFormat;
	}

	@Override
	public String toString() {
		return "Graph(name=\"" + name + "\",path=\"" + relativeFilePath +
				"\",format=" + graphFormat + ")";
	}
}
