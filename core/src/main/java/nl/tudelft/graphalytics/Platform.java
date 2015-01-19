package nl.tudelft.graphalytics;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.Graph;

/**
 * The common interface for any platform that implements the Graphalytics benchmark suite. It
 * defines the API that must be provided by a platform to be compatible with the Graphalytics
 * benchmark driver. The driver uses the {@link #uploadGraph(Graph, String) uploadGraph} and
 * {@link #deleteGraph(String) deleteGraph} functions to ensure the right graphs are loaded,
 * and uses {@link #executeAlgorithmOnGraph(Algorithm, Graph, Object) executeAlgorithmOnGraph}
 * to trigger the executing of various algorithms on each graph.
 *
 * @author Tim Hegeman
 */
public interface Platform {

	/**
	 * Called before executing algorithms on a graph to allow the platform driver to import a graph.
	 * This may include uploading to a distributed filesystem, importing in a graph database, etc.
	 * The platform driver must ensure that this dataset remains available for multiple calls to
	 * {@link #executeAlgorithmOnGraph(Algorithm, Graph, Object) executeAlgorithmOnGraph}, until
	 * the removal of the graph is triggered using {@link #deleteGraph(String) deleteGraph}.
	 *
	 * @param graph information on the graph to be uploaded
	 * @param graphFilePath the path of the graph data
	 * @throws Exception if any exception occurred during the upload
	 */
	void uploadGraph(Graph graph, String graphFilePath) throws Exception;

	/**
	 * Called to trigger the executing of an algorithm on a specific graph. The execution of this
	 * method is timed as part of the benchmarking process. The benchmark driver guarantees that the
	 * graph has been uploaded using the {@link #uploadGraph(Graph, String) uploadGraph} method, and
	 * that it has not been removed by a corresponding call to {@link #deleteGraph(String)
	 * deleteGraph}.
	 *
	 * @param algorithm the algorithm to execute
	 * @param graph the graph to execute the algorithm on
	 * @param parameters the algorithm- and graph-specific parameters
	 * @return true iff the algorithm completed successfully
	 */
	boolean executeAlgorithmOnGraph(Algorithm algorithm, Graph graph, Object parameters);

	/**
	 * Called by the benchmark driver to signal when a graph may be removed from the system. The
	 * driver guarantees that every graph that is uploaded using {@link #uploadGraph(Graph, String)
	 * uploadGraph} is removed using exactly one corresponding call to this method.
	 *
	 * @param graphName the name of the graph to remove (see {@link Graph#getName()})
	 */
	void deleteGraph(String graphName);

	/**
	 * A unique identifier for the platform, used to name benchmark results, etc.
	 * This should be the same as the platform name used to compile and run the benchmark
	 * for this platform, for consistency.
	 *
	 * @return the unique name of the platform
	 */
	public String getName();
	
}
