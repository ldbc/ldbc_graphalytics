package org.tudelft.graphalytics.giraph.stats;

import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexOutputFormat;
import org.apache.giraph.io.formats.IdWithValueTextOutputFormat;
import org.tudelft.graphalytics.algorithms.STATSParameters;
import org.tudelft.graphalytics.giraph.GiraphJob;

public class StatsJob extends GiraphJob {

	public static final String COLLECTION_NODE_KEY = "STATS.COLLECTION_NODE";
	public static final LongConfOption COLLECTION_NODE = new LongConfOption(
			COLLECTION_NODE_KEY, -1, "Id of node that aggregates the results.");
	
	private boolean directed;
	private STATSParameters parameters;
	
	public StatsJob(String inputPath, String outputPath, String zooKeeperAddress,
			Object parameters, boolean directed) {
		super(inputPath, outputPath, zooKeeperAddress);
		this.directed = directed;
		
		assert (parameters instanceof STATSParameters);
		this.parameters = (STATSParameters)parameters;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends BasicComputation> getComputationClass() {
		return (directed ?
			DirectedStatsComputation.class :
			UndirectedStatsComputation.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends VertexInputFormat> getVertexInputFormatClass() {
		return StatsVertexInputFormat.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends VertexOutputFormat> getVertexOutputFormatClass() {
		return IdWithValueTextOutputFormat.class;
	}

	@Override
	protected void configure(GiraphConfiguration config) {
		COLLECTION_NODE.set(config, parameters.getCollectionNode());
	}

}
