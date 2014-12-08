package org.tudelft.graphalytics.giraph.stats;

import org.apache.giraph.aggregators.TextAggregatorWriter;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.Computation;
import org.apache.giraph.io.EdgeInputFormat;
import org.apache.giraph.io.EdgeOutputFormat;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexOutputFormat;
import org.apache.giraph.io.formats.IdWithValueTextOutputFormat;
import org.tudelft.graphalytics.GraphFormat;
import org.tudelft.graphalytics.giraph.GiraphJob;
import org.tudelft.graphalytics.giraph.io.DirectedLongNullTextEdgeInputFormat;
import org.tudelft.graphalytics.giraph.io.UndirectedLongNullTextEdgeInputFormat;

/**
 * The job configuration of the statistics (LCC) implementation for Giraph.
 * 
 * @author Tim Hegeman
 */
public class StatsJob extends GiraphJob {

	private GraphFormat graphFormat;
	
	/**
	 * Constructs a statistics (LCC) job with a graph format specification.
	 * 
	 * @param graphFormat the graph format specification
	 */
	public StatsJob(GraphFormat graphFormat) {
		this.graphFormat = graphFormat;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Computation> getComputationClass() {
		return (graphFormat.isDirected() ?
				DirectedStatsComputation.class :
				UndirectedStatsComputation.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends VertexInputFormat> getVertexInputFormatClass() {
		return graphFormat.isVertexBased() ?
				(graphFormat.isDirected() ?
					DirectedStatsVertexInputFormat.class :
					UndirectedStatsVertexInputFormat.class) :
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
		// Set the master compute class to handle LCC aggregation
		config.setMasterComputeClass(StatsMasterComputation.class);
		config.setAggregatorWriterClass(TextAggregatorWriter.class);
		config.setInt(TextAggregatorWriter.FREQUENCY, TextAggregatorWriter.AT_THE_END);
		config.set(TextAggregatorWriter.FILENAME, getOutputPath() + "/aggregators");
	}

}
