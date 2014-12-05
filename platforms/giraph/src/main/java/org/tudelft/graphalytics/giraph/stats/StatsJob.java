package org.tudelft.graphalytics.giraph.stats;

import org.apache.giraph.aggregators.TextAggregatorWriter;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexOutputFormat;
import org.apache.giraph.io.formats.IdWithValueTextOutputFormat;
import org.tudelft.graphalytics.giraph.GiraphJob;

public class StatsJob extends GiraphJob {

	private boolean directed;
	
	public StatsJob(String inputPath, String outputPath, String zooKeeperAddress,
			Object parameters, boolean directed) {
		super(inputPath, outputPath, zooKeeperAddress);
		this.directed = directed;
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
		return (directed ?
				DirectedStatsVertexInputFormat.class :
				UndirectedStatsVertexInputFormat.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends VertexOutputFormat> getVertexOutputFormatClass() {
		return IdWithValueTextOutputFormat.class;
	}

	@Override
	protected void configure(GiraphConfiguration config) {
		// Set the master compute class to handle LCC aggregation
		config.setMasterComputeClass(StatsMasterComputation.class);
		config.setAggregatorWriterClass(TextAggregatorWriter.class);
		config.setInt(TextAggregatorWriter.FREQUENCY, TextAggregatorWriter.AT_THE_END);
		config.set(TextAggregatorWriter.FILENAME, getOutputPath() + "/aggregators");
	}

}
