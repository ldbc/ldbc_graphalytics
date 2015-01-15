package nl.tudelft.graphalytics.giraph.evo;

import org.apache.giraph.conf.FloatConfOption;
import org.apache.giraph.conf.IntConfOption;
import org.apache.giraph.conf.LongConfOption;

/**
 * Configuration constants for the forest fire model on Giraph.
 * 
 * @author Tim Hegeman
 */
public final class ForestFireModelConfiguration {

	/** Configuration key for the number of new vertices. */
	public static final String NEW_VERTICES_KEY = "graphalytics.evo.new-vertices";
	/** The number of new vertices to create during the forest fire model simulation. */
	public static final LongConfOption NEW_VERTICES = new LongConfOption(
			NEW_VERTICES_KEY, 0, "Number of new vertices to create and connect to the graph.");
	
	/** Configuration key for the lowest available vertex ID. */
	public static final String AVAILABLE_VERTEX_ID_KEY = "graphalytics.evo.available-vertex-id";
	/**
	 * A lower bound on the available vertex IDs for the forest fire model simulation.
	 * All equal or higher IDs must be unused in the input graph.
	 */
	public static final LongConfOption AVAILABLE_VERTEX_ID = new LongConfOption(
			AVAILABLE_VERTEX_ID_KEY, 0,
			"First vertex ID to assign to a created vertex (all higher IDs must also be available).");
	
	/** Configuration key for the maximum number of iterations. */
	public static final String MAX_ITERATIONS_KEY = "graphalytics.evo.max-iterations";
	/** The maximum number of iterations to run the forest fire model algorithm for. */
	public static final IntConfOption MAX_ITERATIONS = new IntConfOption(
			MAX_ITERATIONS_KEY, 1, "Maximum number of iterations to run the forest fire model algorithm for.");
	
	/** Configuration key for the forward burning probability. */
	public static final String FORWARD_PROBABILITY_KEY = "graphalytics.evo.forward-probability";
	/**
	 * The forward burning probability parameter of the forest fire model algorithm.
	 * Must be a value between 0 and 1.
	 */
	public static final FloatConfOption FORWARD_PROBABILITY = new FloatConfOption(
			FORWARD_PROBABILITY_KEY, 0.1f, "Probability of burning a new vertex via an outlink.");
	
	/** Configuration key for the backward burning probability. */
	public static final String BACKWARD_PROBABILITY_KEY = "graphalytics.evo.backward-probability";
	/**
	 * The backward burning probability parameter of the forest fire model algorithm.
	 * Must be a value between 0 and 1.
	 */
	public static final FloatConfOption BACKWARD_PROBABILITY = new FloatConfOption(
			BACKWARD_PROBABILITY_KEY, 0.1f, "Probability of burning a new vertex via an inlink.");

	private ForestFireModelConfiguration() {
	}
	
}
