package org.tudelft.graphalytics.graphx

import org.apache.spark.rdd.RDD
import org.apache.spark.graphx.{VertexId, Graph}
import org.tudelft.graphalytics.GraphFormat
import scala.reflect.ClassTag

object GraphLoader {

	def loadGraph[VD : ClassTag](graphData : RDD[String], graphFormat : GraphFormat,
			defaultValue : VD) : Graph[VD, Int] =
		Graph.fromEdgeTuples(loadEdges(graphFormat)(graphData), defaultValue)
	
	private def loadEdges(graphFormat : GraphFormat) : RDD[String] => RDD[(VertexId, VertexId)] = {
		(graphFormat.isEdgeBased(), graphFormat.isDirected()) match {
			case (false, false) => loadEdgesFromVertexData
			case (false, true) => loadEdgesFromVertexData
			case (true, true) => loadEdgesFromEdgeDirectedData
			case (true, false) => loadEdgesFromEdgeUndirectedData
		}
	}
	
	private def loadEdgesFromEdgeDirectedData(graphData : RDD[String]) : RDD[(VertexId, VertexId)] = {
		def lineToEdge(s : String) : (VertexId, VertexId) = {
			val tokens = s.trim.split("""\s""")
			(tokens(0).toLong, tokens(1).toLong)
		}
		
		graphData.map(lineToEdge)
	}
	
	private def loadEdgesFromEdgeUndirectedData(graphData : RDD[String]) : RDD[(VertexId, VertexId)] = {
		def lineToEdges(s : String) : Array[(VertexId, VertexId)] = {
			val tokens = s.trim.split("""\s""")
			Array((tokens(0).toLong, tokens(1).toLong), (tokens(1).toLong, tokens(0).toLong))
		}
		
		graphData.flatMap(lineToEdges)
	}
	
	private def loadEdgesFromVertexData(graphData : RDD[String]) : RDD[(VertexId, VertexId)] = {
		def lineToEdges(s : String) : Array[(VertexId, VertexId)] = {
			val tokens = s.trim.split("""\s""")
			val src = tokens(0).toLong
			
			tokens.tail.map(s => (src, s.toLong))
		}
		
		graphData.flatMap(lineToEdges)
	}
	
}