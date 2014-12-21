package org.tudelft.graphalytics.graphx

import org.apache.spark.graphx.{VertexId, Graph}
import org.tudelft.graphalytics.GraphFormat
import scala.reflect.ClassTag
import org.apache.spark.graphx.EdgeTriplet

/**
 * Specialisation of GraphXJob for Pregel computations.
 */
abstract class GraphXPregelJob[VD : ClassTag, ED : ClassTag, MSG : ClassTag]
		(graphPath : String, graphFormat : GraphFormat, outputPath : String)
		extends GraphXJob[VD, ED](graphPath, graphFormat, outputPath) {

	/**
	 * Executes the GraphX Pregel computation using functions provided by the
	 * subclass.
	 */
	def compute(graph : Graph[Boolean, Int]) =
		preprocess(graph).pregel(getInitialMessage, getMaxIterations)(vertexProgram, sendMsg, mergeMsg).cache()
	
	/**
	 * Preprocess the parsed graph (with default vertex and edge values) to a
	 * graph with correct initial values.
	 * 
	 * @param graph input graph
	 * @return preprocessed graph
	 */
	def preprocess(graph : Graph[Boolean, Int]) : Graph[VD, ED]
			
	/**
	 * Pregel vertex program. Computes a new vertex value based for a given
	 * vertex ID, the old value of the vertex, and aggregated messages.
	 *
	 * @return the new value of the vertex
	 */
	def vertexProgram : (VertexId, VD, MSG) => VD
	
	/**
	 * Pregel message generation. Produces for each edge a set of messages.
	 * 
	 * @return a set of messages to send
	 */
	def sendMsg : (EdgeTriplet[VD, ED]) => Iterator[(VertexId, MSG)]
	
	/**
	 * Pregel messasge combiner. Merges two messages for the same vertex to a
	 * single message.
	 * 
	 * @return the aggregated message
	 */
	def mergeMsg : (MSG, MSG) => MSG
	
	/**
	 * @return initial message to send to all vertices
	 */
	def getInitialMessage : MSG
	
	/**
	 * @return the maximum number of iterations to run the Pregel algorithm for.
	 */
	def getMaxIterations = Int.MaxValue
	
}