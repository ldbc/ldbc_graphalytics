package org.tudelft.graphalytics.graphx

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.graphx.{VertexId, Graph}
import org.tudelft.graphalytics.GraphFormat
import org.apache.spark.storage.StorageLevel
import scala.reflect.ClassTag
import org.apache.spark.graphx.EdgeTriplet
import org.tudelft.graphalytics.graphx.bfs.BFSJob
import org.tudelft.graphalytics.algorithms.BFSParameters

abstract class GraphXJob[VD : ClassTag, MSG : ClassTag](graphPath : String, graphFormat : GraphFormat) {

	val sparkConfiguration = new SparkConf()
	sparkConfiguration.setAppName(s"Graphalytics: ${getAppName}")
	sparkConfiguration.setMaster("yarn-cluster://localhost")
	val sparkContext = new SparkContext(sparkConfiguration)

	def runJob = {
		// Load the raw graph data
		val graphData : RDD[String] = sparkContext.textFile(graphPath)
		// Parse the vertex and edge data
		val graph = 
			if (useDefaultValue) GraphLoader.loadGraph(graphData, graphFormat, getDefaultValue).cache()
			else GraphLoader.loadGraph(graphData, graphFormat, false)
					.mapVertices((vid, _) => getInitialValue(vid)).cache()

		// Run the Pregel computation
		val output = graph.pregel(getInitialMessage)(vertexProgram, sendMsg, mergeMsg).cache()
		
		// TEMP: Output graph
		output.vertices.collect().foreach {
				vertex => println(s"${vertex._1} ${vertex._2}")
			}
	}
	
	/**
	 * @return true iff the input is valid
	 */
	def hasValidInput : Boolean
	
	/**
	 * Pregel vertex program. Computes a new vertex value based for a given
	 * vertex ID, the old value of the vertex, and aggregated messages.
	 * 
	 * @param vertexId the ID of the vertex
	 * @param oldValue the old value of the vertex
	 * @param message the result of the message aggregation
	 * @return the new value of the vertex
	 */
	def vertexProgram(vertexId : VertexId, oldValue : VD, message : MSG) : VD
	
	/**
	 * Pregel message generation. Produces for each edge a set of messages.
	 * 
	 * @param edgeData a triplet representing an edge
	 * @return a set of messages to send
	 */
	def sendMsg(edgeData: EdgeTriplet[VD, Int]) : Iterator[(VertexId, MSG)]
	
	/**
	 * Pregel messasge combiner. Merges to messages for the same vertex to a
	 * single message.
	 * 
	 * @param messageA first message
	 * @param messageB second message
	 * @return the aggregated message
	 */
	def mergeMsg(messageA : MSG, messageB : MSG) : MSG
	
	/**
	 * @return initial message to send to all vertices
	 */
	def getInitialMessage : MSG
	
	/**
	 * @return name of the GraphX job
	 */
	def getAppName : String

	/**
	 * @return true iff all vertices should start with the same default value
	 */
	def useDefaultValue : Boolean
	
	/**
	 * @return the default vertex value, if used
	 */
	def getDefaultValue : VD
	
	/**
	 * @param vertexId ID of a vertex
	 * @return the initial value corresponding with vertexID
	 */
	def getInitialValue(vertexId : VertexId) : VD
	
}

//object GraphXJob {
//	// Parameters: (directed|undirected) (vertex|edge) graphPath algorithm algorithmParameters
//	def main(args : Array[String]) {
//		val format = new GraphFormat(args(0) == "directed", args(1) == "edge")
//		val graphPath = args(2)
//		
//		val graphxJob = args(3) match {
//			case "BFS" => new BFSJob(graphPath, format, BFSParameters.parse(args(4)))
//		}
//		
//		graphxJob.runJob
//	}
//}