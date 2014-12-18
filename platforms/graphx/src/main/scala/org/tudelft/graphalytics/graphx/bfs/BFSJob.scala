package org.tudelft.graphalytics.graphx.bfs

import org.tudelft.graphalytics.graphx.GraphXJob
import org.tudelft.graphalytics.GraphFormat
import org.apache.spark.graphx.{EdgeTriplet, VertexId}
import org.tudelft.graphalytics.algorithms.BFSParameters
import org.apache.spark.graphx.Graph

class BFSJob(graphPath : String, graphFormat : GraphFormat, parameters : Object) extends
		GraphXJob[Long, Long](graphPath, graphFormat) {

	val bfsParam : BFSParameters = parameters match {
		case p : BFSParameters => p
		case _ => null
	}
	
//	def compute(graph : Graph[Long, Int]) = graph.pregel(Long.MaxValue)(
//				(id, dist, newDist) => if (dist < newDist)  dist else newDist,
//				triplet => {
//					if ((triplet.srcAttr < Long.MaxValue) && (triplet.srcAttr + 1 < triplet.dstAttr))
//						Iterator((triplet.dstId, triplet.srcAttr + 1))
//					else
//						Iterator.empty
//				},
//				(a, b) => math.min(a, b))
	def compute(graph : Graph[Long, Int]) = graph.pregel(getInitialMessage)(vertexProgram, sendMsg, mergeMsg)
	
	/**
	 * @return true iff the input is valid
	 */
	def hasValidInput = parameters match {
		case param : BFSParameters => true
		case _ => false
	}
	
	/**
	 * Pregel vertex program. Computes a new vertex value based for a given
	 * vertex ID, the old value of the vertex, and aggregated messages.
	 * 
	 * @param vertexId the ID of the vertex
	 * @param oldValue the old value of the vertex
	 * @param message the result of the message aggregation
	 * @return the new value of the vertex
	 */
	def vertexProgram(vertexId : VertexId, oldValue : Long, message : Long) =
		math.min(oldValue, message)
	
	/**
	 * Pregel message generation. Produces for each edge a set of messages.
	 * 
	 * @param edgeData a triplet representing an edge
	 * @return a set of messages to send
	 */
	def sendMsg(edgeData: EdgeTriplet[Long, Int]) =
		if (edgeData.srcAttr < Long.MaxValue && edgeData.srcAttr + 1L < edgeData.dstAttr)
			Iterator((edgeData.dstId, edgeData.srcAttr + 1L))
		else
			Iterator.empty
	
	/**
	 * Pregel messasge combiner. Merges to messages for the same vertex to a
	 * single message.
	 * 
	 * @param messageA first message
	 * @param messageB second message
	 * @return the aggregated message
	 */
	def mergeMsg(messageA : Long, messageB : Long) = math.min(messageA, messageB)
	
	/**
	 * @return initial message to send to all vertices
	 */
	def getInitialMessage = Long.MaxValue
	
	/**
	 * @return name of the GraphX job
	 */
	def getAppName = "BFS"

	/**
	 * @return true iff all vertices should start with the same default value
	 */
	def useDefaultValue = false
	
	/**
	 * UNUSED
	 * 
	 * @return the default vertex value, if used
	 */
	def getDefaultValue = 0L
	
	/**
	 * @param vertexId ID of a vertex
	 * @return the initial value corresponding with vertexID
	 */
	def getInitialValue(vertexId : VertexId) =
		if (vertexId == bfsParam.getSourceVertex)
			0L
		else
			Long.MaxValue

}