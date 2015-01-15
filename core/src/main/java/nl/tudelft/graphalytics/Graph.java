package nl.tudelft.graphalytics;

/**
 * Represents a single graph in the Graphalytics benchmark suite. Each graph has
 * a unique name, a path to a file containing the graph data, and a format
 * specification. 
 * 
 * @author Tim Hegeman
 */
public class Graph {

	private String name;
	private String relativeFilePath;
	private GraphFormat graphFormat;
	
	/**
	 * @param name the unique name of the graph
	 * @param relativeFilePath the path of the graph file, relative to the
	 *        graph root directory
	 * @param graphFormat the format of the graph
	 */
	public Graph(String name, String relativeFilePath, GraphFormat graphFormat) {
		this.name = name;
		this.relativeFilePath = relativeFilePath;
		this.graphFormat = graphFormat;
	}
	
	/**
	 * @param name the unique name of the graph
	 * @param relativeFilePath the path of the graph file, relative to the
	 *        graph root directory
	 * @param directed true iff the graph is directed
	 * @param edgeBased true iff the graph is stored in an edge-based format
	 */
	public Graph(String name, String relativeFilePath, boolean directed, boolean edgeBased) {
		this(name, relativeFilePath, new GraphFormat(directed, edgeBased));
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
	public GraphFormat getGraphFormat() {
		return graphFormat;
	}
	
	/**
	 * Convenience accessor for {@link GraphFormat#isDirected()}.
	 * 
	 * @deprecated
	 * @return true iff the graph is directed
	 */
	public boolean isDirected() {
		return getGraphFormat().isDirected();
	}
	
	/**
	 * Convenience accessor for {@link GraphFormat#isEdgeBased()}.
	 * 
	 * @deprecated
	 * @return true iff the graph is edge-based
	 */
	public boolean isEdgeBased() {
		return getGraphFormat().isEdgeBased();
	}
	
	@Override
	public String toString() {
		return "Graph(name=" + name + ",path=" + relativeFilePath +
				",format=" + graphFormat + ")";
	}
	
}
