package org.tudelft.graphalytics.graphx.conn

import org.apache.spark.graphx.{VertexId, Graph}
import org.apache.spark.rdd.RDD
import org.tudelft.graphalytics.GraphFormat
import org.tudelft.graphalytics.graphx.GraphXJob

/**
 * The implementation of (strongly) connected components on GraphX.
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
		if (graphFormat.isDirected)
			graph.stronglyConnectedComponents(Int.MaxValue)
		else
			graph.connectedComponents()

	/**
	 * Convert a graph to the output format of this job.
	 *
	 * @return a RDD of strings (lines of output)
	 */
	override def makeOutput(graph: Graph[VertexId, Int]): RDD[String] =
		graph.vertices.map(vertex => s"${vertex._1} ${vertex._2}")

	/**
	 * @return name of the GraphX job
	 */
	override def getAppName: String = "Connected Components"

	/**
	 * @return true iff the input is valid
	 */
	override def hasValidInput: Boolean = true
}
