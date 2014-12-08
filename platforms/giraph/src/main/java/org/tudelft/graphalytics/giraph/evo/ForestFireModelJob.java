package org.tudelft.graphalytics.giraph.evo;

import static org.tudelft.graphalytics.giraph.evo.ForestFireModelConfiguration.AVAILABLE_VERTEX_ID;
import static org.tudelft.graphalytics.giraph.evo.ForestFireModelConfiguration.BACKWARD_PROBABILITY;
import static org.tudelft.graphalytics.giraph.evo.ForestFireModelConfiguration.FORWARD_PROBABILITY;
import static org.tudelft.graphalytics.giraph.evo.ForestFireModelConfiguration.MAX_ITERATIONS;
import static org.tudelft.graphalytics.giraph.evo.ForestFireModelConfiguration.NEW_VERTICES;

import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.Computation;
import org.apache.giraph.io.EdgeInputFormat;
import org.apache.giraph.io.EdgeOutputFormat;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexOutputFormat;
import org.tudelft.graphalytics.GraphFormat;
import org.tudelft.graphalytics.algorithms.EVOParameters;
import org.tudelft.graphalytics.giraph.GiraphJob;
import org.tudelft.graphalytics.giraph.io.DirectedLongNullTextEdgeInputFormat;
import org.tudelft.graphalytics.giraph.io.UndirectedLongNullTextEdgeInputFormat;

/**
 * Job configuration of the forest fire model implementation for Giraph.
 * 
 * @author Tim Hegeman
 */
public class ForestFireModelJob extends GiraphJob {

	private EVOParameters parameters;
	private GraphFormat graphFormat;
	
	/**
	 * Constructs a forest fire model job with a EVOParameters object containing
	 * graph-specific parameters, and a graph format specification
	 * 
	 * @param parameters the graph-specific EVO parameters
	 * @param graphFormat the graph format specification
	 */
	public ForestFireModelJob(Object parameters, GraphFormat graphFormat) {
		assert (parameters instanceof EVOParameters);
		this.parameters = (EVOParameters)parameters;
		this.graphFormat = graphFormat;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Computation> getComputationClass() {
		return (graphFormat.isDirected() ?
				DirectedForestFireModelComputation.class :
				UndirectedForestFireModelComputation.class);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends VertexInputFormat> getVertexInputFormatClass() {
		return graphFormat.isVertexBased() ?
				ForestFireModelVertexInputFormat.class :
				null;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends VertexOutputFormat> getVertexOutputFormatClass() {
		return AdjacencyListWithoutValuesVertexOutputFormat.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends EdgeInputFormat> getEdgeInputFormatClass() {
		return graphFormat.isEdgeBased() ?
				(graphFormat.isDirected() ?
					DirectedLongNullTextEdgeInputFormat.class :
					UndirectedLongNullTextEdgeInputFormat.class) :
				null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends EdgeOutputFormat> getEdgeOutputFormatClass() {
		return null;
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
