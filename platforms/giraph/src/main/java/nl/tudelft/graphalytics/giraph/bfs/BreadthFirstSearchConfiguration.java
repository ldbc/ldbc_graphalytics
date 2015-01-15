package nl.tudelft.graphalytics.giraph.bfs;

import org.apache.giraph.conf.LongConfOption;

/**
 * Configuration constants for breadth-first search on Giraph.
 * 
 * @author Tim Hegeman
 */
public final class BreadthFirstSearchConfiguration {
	
	/** Configuration key for the source vertex of the algorithm */
	public static final String SOURCE_VERTEX_KEY = "graphalytics.bfs.source-vertex";
	/** Configuration option for the source vertex of the algorithm */
	public static final LongConfOption SOURCE_VERTEX = new LongConfOption(
			SOURCE_VERTEX_KEY, -1, "Source vertex for the breadth first search algorithm");

	private BreadthFirstSearchConfiguration() {
	}
	
}
