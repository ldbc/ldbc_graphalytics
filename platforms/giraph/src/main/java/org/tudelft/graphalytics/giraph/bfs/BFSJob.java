package org.tudelft.graphalytics.giraph.bfs;

import static org.tudelft.graphalytics.giraph.bfs.BFSConfiguration.SOURCE_VERTEX;

import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.Computation;
import org.apache.giraph.io.EdgeInputFormat;
import org.apache.giraph.io.EdgeOutputFormat;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexOutputFormat;
import org.apache.giraph.io.formats.IdWithValueTextOutputFormat;
import org.apache.giraph.io.formats.LongLongNullTextInputFormat;
import org.tudelft.graphalytics.GraphFormat;
import org.tudelft.graphalytics.algorithms.BFSParameters;
import org.tudelft.graphalytics.giraph.GiraphJob;
import org.tudelft.graphalytics.giraph.io.DirectedLongNullTextEdgeInputFormat;
import org.tudelft.graphalytics.giraph.io.UndirectedLongNullTextEdgeInputFormat;

/**
 * The job configuration of the breadth-first-search implementation for Giraph.  
 * 
 * @author Tim Hegeman
 */
public class BFSJob extends GiraphJob {

	private BFSParameters parameters;
	private GraphFormat graphFormat;
	
	/**
	 * Constructs a breadth-first-search job with a BFSParameters object containing
	 * graph-specific parameters, and a graph format specification
	 * 
	 * @param parameters the graph-specific BFS parameters
	 * @param graphFormat the graph format specification
	 */
	public BFSJob(Object parameters, GraphFormat graphFormat) {
		assert (parameters instanceof BFSParameters);
		this.parameters = (BFSParameters)parameters;
		this.graphFormat = graphFormat;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Computation> getComputationClass() {
		return BFSComputation.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends VertexInputFormat> getVertexInputFormatClass() {
		return graphFormat.isVertexBased() ?
				LongLongNullTextInputFormat.class :
				null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends VertexOutputFormat> getVertexOutputFormatClass() {
		return IdWithValueTextOutputFormat.class;
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
		SOURCE_VERTEX.set(config, parameters.getSourceVertex());
	}

}
