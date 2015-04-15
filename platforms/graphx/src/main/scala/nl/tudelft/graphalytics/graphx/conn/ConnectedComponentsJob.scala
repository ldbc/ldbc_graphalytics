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
package nl.tudelft.graphalytics.graphx.conn

import nl.tudelft.graphalytics.domain.GraphFormat
import org.apache.spark.graphx.{VertexId, Graph}
import org.apache.spark.rdd.RDD
import nl.tudelft.graphalytics.graphx.{GraphXJobOutput, GraphXJob}

/**
 * The implementation of (strongly) connected components on GraphX.
 *
 * @param graphPath the input path of the graph
 * @param graphFormat the format of the graph data
 * @param outputPath the output path of the computation
 * @author Tim Hegeman
 */
class ConnectedComponentsJob(graphPath : String, graphFormat : GraphFormat, outputPath : String)
		extends GraphXJob[VertexId, Int](graphPath, graphFormat, outputPath) {

	/**
	 * Perform the graph computation using job-specific logic.
	 *
	 * @param graph the parsed graph with default vertex and edge values
	 * @return the resulting graph after the computation
	 */
	override def compute(graph: Graph[Boolean, Int]): Graph[VertexId, Int] =
		graph.connectedComponents()

	/**
	 * Convert a graph to the output format of this job.
	 *
	 * @return a GraphXJobOutput object representing the job result
	 */
	override def makeOutput(graph: Graph[VertexId, Int]) =
		new GraphXJobOutput(graph.vertices.map(vertex => s"${vertex._1} ${vertex._2}").cache())

	/**
	 * @return name of the GraphX job
	 */
	override def getAppName: String = "Connected Components"

	/**
	 * @return true iff the input is valid
	 */
	override def hasValidInput: Boolean = true
}
