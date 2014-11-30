package org.tudelft.graphalytics.giraph.evo;

import org.apache.giraph.conf.FloatConfOption;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.conf.IntConfOption;
import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexOutputFormat;
import org.tudelft.graphalytics.algorithms.EVOParameters;
import org.tudelft.graphalytics.giraph.GiraphJob;

public class ForestFireModelJob extends GiraphJob {

	public static final String NEW_VERTICES_KEY = "EVO.NEW_VERTICES";
	public static final LongConfOption NEW_VERTICES = new LongConfOption(
			NEW_VERTICES_KEY, 0, "Number of new vertices to create and connect to the graph.");
	
	public static final String AVAILABLE_VERTEX_ID_KEY = "EVO.AVAILABLE_VERTEX_ID";
	public static final LongConfOption AVAILABLE_VERTEX_ID = new LongConfOption(
			AVAILABLE_VERTEX_ID_KEY, 0,
			"First vertex ID to assign to a created vertex (all higher IDs must also be available).");
	
	public static final String MAX_ITERATIONS_KEY = "EVO.MAX_ITERATIONS";
	public static final IntConfOption MAX_ITERATIONS = new IntConfOption(
			MAX_ITERATIONS_KEY, 1, "Maximum number of iterations to run the forest fire model algorithm for.");
	
	public static final String FORWARD_PROBABILITY_KEY = "EVO.FORWARD_PROBABILITY";
	public static final FloatConfOption FORWARD_PROBABILITY = new FloatConfOption(
			FORWARD_PROBABILITY_KEY, 0.1f, "Probability of burning a new vertex via an outlink.");
	
	public static final String BACKWARD_PROBABILITY_KEY = "EVO.BACKWARD_PROBABILITY";
	public static final FloatConfOption BACKWARD_PROBABILITY = new FloatConfOption(
			BACKWARD_PROBABILITY_KEY, 0.1f, "Probability of burning a new vertex via an inlink.");
	
	private EVOParameters parameters;
	private boolean directed;
	
	public ForestFireModelJob(String inputPath, String outputPath,
			String zooKeeperAddress, Object parameters, boolean directed) {
		super(inputPath, outputPath, zooKeeperAddress);
		this.directed = directed;
		
		assert (parameters instanceof EVOParameters);
		this.parameters = (EVOParameters)parameters;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends BasicComputation> getComputationClass() {
		return (directed ?
				DirectedForestFireModelComputation.class :
				null);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends VertexInputFormat> getVertexInputFormatClass() {
		return ForestFireModelVertexInputFormat.class;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends VertexOutputFormat> getVertexOutputFormatClass() {
		return AdjacencyListWithoutValuesVertexOutputFormat.class;
	}
	
	@Override
	protected void configure(GiraphConfiguration config) {
		NEW_VERTICES.set(config, parameters.getNumNewVertices());
		AVAILABLE_VERTEX_ID.set(config, parameters.getMaxId() + 1);
		MAX_ITERATIONS.set(config, parameters.getMaxIterations());
		FORWARD_PROBABILITY.set(config, parameters.getPRatio());
		BACKWARD_PROBABILITY.set(config, parameters.getRRatio());
		
		config.setWorkerContextClass(ForestFireModelWorkerContext.class);
	}
	
}
