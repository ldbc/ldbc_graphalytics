package org.tudelft.graphalytics.graphx

import org.apache.spark.graphx.{VertexId, Graph}
import org.tudelft.graphalytics.GraphFormat
import org.apache.spark.storage.StorageLevel
import scala.reflect.ClassTag
import org.apache.spark.graphx.EdgeTriplet

/**
 * Specialisation of GraphXJob for Pregel computations.
 */
abstract class GraphXPregelJob[VD : ClassTag, MSG : ClassTag](graphPath : String, 
		graphFormat : GraphFormat, outputPath : String)
		extends GraphXJob[VD](graphPath, graphFormat, outputPath) {

	/**
	 * Executes the GraphX Pregel computation using functions provided by the
	 * subclass.
	 */
	def compute(graph : Graph[VD, Int]) =
		graph.pregel(getInitialMessage)(vertexProgram, sendMsg, mergeMsg).cache()
	
	/**
	 * Pregel vertex program. Computes a new vertex value based for a given
	 * vertex ID, the old value of the vertex, and aggregated messages.
	 * 
	 * @param vertexId the ID of the vertex
	 * @param oldValue the old value of the vertex
	 * @param message the result of the message aggregation
	 * @return the new value of the vertex
	 */
	def vertexProgram : (VertexId, VD, MSG) => VD
	
	/**
	 * Pregel message generation. Produces for each edge a set of messages.
	 * 
	 * @param edgeData a triplet representing an edge
	 * @return a set of messages to send
	 */
	def sendMsg : (EdgeTriplet[VD, Int]) => Iterator[(VertexId, MSG)]
	
	/**
	 * Pregel messasge combiner. Merges two messages for the same vertex to a
	 * single message.
	 * 
	 * @param messageA first message
	 * @param messageB second message
	 * @return the aggregated message
	 */
	def mergeMsg : (MSG, MSG) => MSG
	
	/**
	 * @return initial message to send to all vertices
	 */
	def getInitialMessage : MSG
	
}