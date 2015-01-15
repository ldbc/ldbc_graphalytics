package nl.tudelft.graphalytics.giraph.stats;

import org.apache.giraph.aggregators.TextAggregatorWriter;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.Computation;
import org.apache.giraph.io.EdgeInputFormat;
import org.apache.giraph.io.EdgeOutputFormat;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexOutputFormat;
import org.apache.giraph.io.formats.IdWithValueTextOutputFormat;
import nl.tudelft.graphalytics.GraphFormat;
import nl.tudelft.graphalytics.giraph.GiraphJob;
import nl.tudelft.graphalytics.giraph.io.DirectedLongNullTextEdgeInputFormat;
import nl.tudelft.graphalytics.giraph.io.UndirectedLongNullTextEdgeInputFormat;

/**
 * The job configuration of the statistics (LCC) implementation for Giraph.
 * 
 * @author Tim Hegeman
 */
public class LocalClusteringCoefficientJob extends GiraphJob {

	private GraphFormat graphFormat;
	
	/**
	 * Constructs a statistics (LCC) job with a graph format specification.
	 * 
	 * @param graphFormat the graph format specification
	 */
	public LocalClusteringCoefficientJob(GraphFormat graphFormat) {
		this.graphFormat = graphFormat;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Computation> getComputationClass() {
		return (graphFormat.isDirected() ?
				DirectedLocalClusteringCoefficientComputation.class :
				UndirectedLocalClusteringCoefficientComputation.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends VertexInputFormat> getVertexInputFormatClass() {
		return graphFormat.isVertexBased() ?
				(graphFormat.isDirected() ?
					DirectedLocalClusteringCoefficientVertexInputFormat.class :
					UndirectedLocalClusteringCoefficientVertexInputFormat.class) :
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
		config.setMasterComputeClass(LocalClusteringCoefficientMasterComputation.class);
		config.setAggregatorWriterClass(TextAggregatorWriter.class);
		config.setInt(TextAggregatorWriter.FREQUENCY, TextAggregatorWriter.AT_THE_END);
		config.set(TextAggregatorWriter.FILENAME, getOutputPath() + "/aggregators");
	}

}
