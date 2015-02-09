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
