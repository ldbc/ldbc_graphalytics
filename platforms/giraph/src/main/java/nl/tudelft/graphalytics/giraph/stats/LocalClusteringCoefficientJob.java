/**
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics.giraph.stats;

import nl.tudelft.graphalytics.domain.GraphFormat;
import org.apache.giraph.aggregators.TextAggregatorWriter;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.Computation;
import org.apache.giraph.io.EdgeInputFormat;
import org.apache.giraph.io.EdgeOutputFormat;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexOutputFormat;
import org.apache.giraph.io.formats.IdWithValueTextOutputFormat;
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
		return !graphFormat.isEdgeBased() ?
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
