package org.tudelft.graphalytics.giraph.cd;

import org.apache.giraph.conf.FloatConfOption;
import org.apache.giraph.conf.IntConfOption;

/**
 * Configuration constants for community detection on Giraph.
 * 
 * @author Tim Hegeman
 */
public final class CommunityDetectionConfiguration {

	/** Configuration key for the node preference of the algorithm. */
	public static final String NODE_PREFERENCE_KEY = "graphalytics.cd.node-preference";
	/** The node preference parameter of the community detection algorithm. */
	public static final FloatConfOption NODE_PREFERENCE = new FloatConfOption(
			NODE_PREFERENCE_KEY, 1.0f, "Node preference parameter for community detection.");
	
	/** Configuration key for the hop attenuation of the algorithm. */
	public static final String HOP_ATTENUATION_KEY = "graphalytics.cd.hop-attenuation";
	/** The hop attenuation parameter of the community detection algorithm. */
	public static final FloatConfOption HOP_ATTENUATION = new FloatConfOption(
			HOP_ATTENUATION_KEY, 1.0f, "Hop attenuation parameter for community detection.");
	
	/** Configuration key for the maximum number of iterations of the algorithm. */
	public static final String MAX_ITERATIONS_KEY = "graphalytics.cd.max-iterations";
	/** The maximum number of iterations to run the community detection algorithm for. */
	public static final IntConfOption MAX_ITERATIONS = new IntConfOption(
			MAX_ITERATIONS_KEY, 1, "Maximum number of iterations to run the community detection algorithm for.");

	private CommunityDetectionConfiguration() {
	}
	
}
