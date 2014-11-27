package org.tudelft.graphalytics.giraph.cd;

import org.apache.giraph.conf.FloatConfOption;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.conf.IntConfOption;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexOutputFormat;
import org.apache.giraph.io.formats.IdWithValueTextOutputFormat;
import org.apache.giraph.io.formats.LongLongNullTextInputFormat;
import org.tudelft.graphalytics.algorithms.CDParameters;
import org.tudelft.graphalytics.giraph.GiraphJob;

public class CommunityDetectionJob extends GiraphJob {
	//weight factor for arbitrary comparable characteristics f(i)
	public static final String NODE_PREFERENCE_KEY = "CD.NODE_PREFERENCE";
	public static final FloatConfOption NODE_PREFERENCE = new FloatConfOption(
			NODE_PREFERENCE_KEY, 1.0f, "Node preference parameter for community detection.");
	
	//hop attenuation betwen 0 and 1
	public static final String HOP_ATTENUATION_KEY = "CD.HOP_ATTENUATUION";
	public static final FloatConfOption HOP_ATTENUATION = new FloatConfOption(
			HOP_ATTENUATION_KEY, 1.0f, "Hop attenuation parameter for community detection.");
	
	//max iterations before stopping
	public static final String MAX_ITERATIONS_KEY = "CD.MAX_ITERATIONS";
	public static final IntConfOption MAX_ITERATIONS = new IntConfOption(
			MAX_ITERATIONS_KEY, 1, "Maximum number of iterations to run the community detection algorithm for.");
	
	private CDParameters parameters;
	private boolean directed;
	
	public CommunityDetectionJob(String inputPath, String outputPath, String zooKeeperAddress,
			Object parameters, boolean directed) {
		super(inputPath, outputPath, zooKeeperAddress);
		this.directed = directed;
		
		assert (parameters instanceof CDParameters);
		this.parameters = (CDParameters)parameters;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends BasicComputation> getComputationClass() {
		return (directed ?
			DirectedCommunityDetectionComputation.class :
			UndirectedCommunityDetectionComputation.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends VertexInputFormat> getVertexInputFormatClass() {
		return LongLongNullTextInputFormat.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends VertexOutputFormat> getVertexOutputFormatClass() {
		return IdWithValueTextOutputFormat.class;
	}

	@Override
	protected void configure(GiraphConfiguration config) {
		NODE_PREFERENCE.set(config, parameters.getNodePreference());
		HOP_ATTENUATION.set(config, parameters.getHopAttenuation());
		MAX_ITERATIONS.set(config, parameters.getMaxIterations());
	}

}
