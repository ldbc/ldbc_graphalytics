package org.tudelft.graphalytics.giraph.cd;

import static org.tudelft.graphalytics.giraph.cd.CommunityDetectionConfiguration.HOP_ATTENUATION;
import static org.tudelft.graphalytics.giraph.cd.CommunityDetectionConfiguration.MAX_ITERATIONS;
import static org.tudelft.graphalytics.giraph.cd.CommunityDetectionConfiguration.NODE_PREFERENCE;

import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.Computation;
import org.apache.giraph.io.EdgeInputFormat;
import org.apache.giraph.io.EdgeOutputFormat;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexOutputFormat;
import org.apache.giraph.io.formats.IdWithValueTextOutputFormat;
import org.tudelft.graphalytics.GraphFormat;
import org.tudelft.graphalytics.algorithms.CDParameters;
import org.tudelft.graphalytics.giraph.GiraphJob;
import org.tudelft.graphalytics.giraph.io.DirectedLongNullTextEdgeInputFormat;
import org.tudelft.graphalytics.giraph.io.UndirectedLongNullTextEdgeInputFormat;

/**
 * The job configuration of the community detection implementation for Giraph.
 * 
 * @author Tim Hegeman
 */
public class CommunityDetectionJob extends GiraphJob {
	
	private CDParameters parameters;
	private GraphFormat graphFormat;

	/**
	 * Constructs a community detection job with a CDParameters object containing
	 * graph-specific parameters, and a graph format specification
	 * 
	 * @param parameters the graph-specific CD parameters
	 * @param graphFormat the graph format specification
	 */
	public CommunityDetectionJob(Object parameters, GraphFormat graphFormat) {
		assert (parameters instanceof CDParameters);
		this.parameters = (CDParameters)parameters;
		this.graphFormat = graphFormat;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Computation> getComputationClass() {
		return (graphFormat.isDirected() ?
			DirectedCommunityDetectionComputation.class :
			UndirectedCommunityDetectionComputation.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends VertexInputFormat> getVertexInputFormatClass() {
		return graphFormat.isVertexBased() ?
				CommunityDetectionVertexInputFormat.class :
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
		NODE_PREFERENCE.set(config, parameters.getNodePreference());
		HOP_ATTENUATION.set(config, parameters.getHopAttenuation());
		MAX_ITERATIONS.set(config, parameters.getMaxIterations());
	}

}
