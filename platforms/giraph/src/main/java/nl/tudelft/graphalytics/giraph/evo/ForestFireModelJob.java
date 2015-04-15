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
package nl.tudelft.graphalytics.giraph.evo;

import static nl.tudelft.graphalytics.giraph.evo.ForestFireModelConfiguration.AVAILABLE_VERTEX_ID;
import static nl.tudelft.graphalytics.giraph.evo.ForestFireModelConfiguration.BACKWARD_PROBABILITY;
import static nl.tudelft.graphalytics.giraph.evo.ForestFireModelConfiguration.FORWARD_PROBABILITY;
import static nl.tudelft.graphalytics.giraph.evo.ForestFireModelConfiguration.MAX_ITERATIONS;
import static nl.tudelft.graphalytics.giraph.evo.ForestFireModelConfiguration.NEW_VERTICES;

import nl.tudelft.graphalytics.domain.GraphFormat;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.Computation;
import org.apache.giraph.io.EdgeInputFormat;
import org.apache.giraph.io.EdgeOutputFormat;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexOutputFormat;
import nl.tudelft.graphalytics.domain.algorithms.ForestFireModelParameters;
import nl.tudelft.graphalytics.giraph.GiraphJob;
import nl.tudelft.graphalytics.giraph.io.DirectedLongNullTextEdgeInputFormat;
import nl.tudelft.graphalytics.giraph.io.UndirectedLongNullTextEdgeInputFormat;

/**
 * Job configuration of the forest fire model implementation for Giraph.
 * 
 * @author Tim Hegeman
 */
public class ForestFireModelJob extends GiraphJob {

	private ForestFireModelParameters parameters;
	private GraphFormat graphFormat;
	
	/**
	 * Constructs a forest fire model job with a EVOParameters object containing
	 * graph-specific parameters, and a graph format specification
	 * 
	 * @param parameters the graph-specific EVO parameters
	 * @param graphFormat the graph format specification
	 */
	public ForestFireModelJob(Object parameters, GraphFormat graphFormat) {
		assert (parameters instanceof ForestFireModelParameters);
		this.parameters = (ForestFireModelParameters)parameters;
		this.graphFormat = graphFormat;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Computation> getComputationClass() {
		return (graphFormat.isDirected() ?
				DirectedForestFireModelComputation.class :
				UndirectedForestFireModelComputation.class);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends VertexInputFormat> getVertexInputFormatClass() {
		return !graphFormat.isEdgeBased() ?
				ForestFireModelVertexInputFormat.class :
				null;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends VertexOutputFormat> getVertexOutputFormatClass() {
		return AdjacencyListWithoutValuesVertexOutputFormat.class;
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
		NEW_VERTICES.set(config, parameters.getNumNewVertices());
		AVAILABLE_VERTEX_ID.set(config, parameters.getMaxId() + 1);
		MAX_ITERATIONS.set(config, parameters.getMaxIterations());
		FORWARD_PROBABILITY.set(config, parameters.getPRatio());
		BACKWARD_PROBABILITY.set(config, parameters.getRRatio());
		
		config.setWorkerContextClass(ForestFireModelWorkerContext.class);
	}
	
}
