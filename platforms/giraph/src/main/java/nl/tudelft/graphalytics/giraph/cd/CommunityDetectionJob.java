package nl.tudelft.graphalytics.giraph.cd;

import static nl.tudelft.graphalytics.giraph.cd.CommunityDetectionConfiguration.HOP_ATTENUATION;
import static nl.tudelft.graphalytics.giraph.cd.CommunityDetectionConfiguration.MAX_ITERATIONS;
import static nl.tudelft.graphalytics.giraph.cd.CommunityDetectionConfiguration.NODE_PREFERENCE;

import nl.tudelft.graphalytics.domain.GraphFormat;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.Computation;
import org.apache.giraph.io.EdgeInputFormat;
import org.apache.giraph.io.EdgeOutputFormat;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexOutputFormat;
import org.apache.giraph.io.formats.IdWithValueTextOutputFormat;
import nl.tudelft.graphalytics.domain.algorithms.CommunityDetectionParameters;
import nl.tudelft.graphalytics.giraph.GiraphJob;
import nl.tudelft.graphalytics.giraph.io.DirectedLongNullTextEdgeInputFormat;
import nl.tudelft.graphalytics.giraph.io.UndirectedLongNullTextEdgeInputFormat;

/**
 * The job configuration of the community detection implementation for Giraph.
 * 
 * @author Tim Hegeman
 */
public class CommunityDetectionJob extends GiraphJob {
	
	private CommunityDetectionParameters parameters;
	private GraphFormat graphFormat;

	/**
	 * Constructs a community detection job with a CDParameters object containing
	 * graph-specific parameters, and a graph format specification
	 * 
	 * @param parameters the graph-specific CD parameters
	 * @param graphFormat the graph format specification
	 */
	public CommunityDetectionJob(Object parameters, GraphFormat graphFormat) {
		assert parameters instanceof CommunityDetectionParameters;
		this.parameters = (CommunityDetectionParameters)parameters;
		this.graphFormat = graphFormat;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Computation> getComputationClass() {
		return graphFormat.isDirected() ?
			DirectedCommunityDetectionComputation.class :
			UndirectedCommunityDetectionComputation.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends VertexInputFormat> getVertexInputFormatClass() {
		return !graphFormat.isEdgeBased() ?
				(graphFormat.isDirected() ?
					CommunityDetectionVertexInputFormat.Directed.class :
					CommunityDetectionVertexInputFormat.Undirected.class) :
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
					DirectedCommunityDetectionEdgeInputFormat.class :
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
