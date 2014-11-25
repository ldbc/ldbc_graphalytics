package org.tudelft.graphalytics.giraph.bfs;

import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexOutputFormat;
import org.apache.giraph.io.formats.IdWithValueTextOutputFormat;
import org.apache.giraph.io.formats.LongLongNullTextInputFormat;
import org.tudelft.graphalytics.giraph.GiraphJob;

public class BFSJob extends GiraphJob {

	public BFSJob(String inputPath, String outputPath) {
		super(inputPath, outputPath);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends BasicComputation> getComputationClass() {
		return BFSComputation.class;
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
		BFSComputation.SOURCE_VERTEX.set(config, 1);
	}
	
}
