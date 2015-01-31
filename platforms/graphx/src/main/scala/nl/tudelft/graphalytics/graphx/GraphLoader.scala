package nl.tudelft.graphalytics.graphx

import nl.tudelft.graphalytics.domain.GraphFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.graphx.{VertexId, Graph}
import scala.reflect.ClassTag

/**
 * Utility class for loading graphs in various formats (directed vs undirected, vertex- vs edge-based).
 */
object GraphLoader {

	/**
	 * @param graphData raw graph data represent as one string per line of input
	 * @param graphFormat the graph data format specification
	 * @param defaultValue default value for each vertex
	 * @tparam VD vertex value type
	 * @return a parsed GraphX graph
	 */
	def loadGraph[VD : ClassTag](graphData : RDD[String], graphFormat : GraphFormat,
			defaultValue : VD) : Graph[VD, Int] =
		Graph.fromEdgeTuples(loadEdges(graphFormat)(graphData), defaultValue)

	/**
	 * Convenience method for selected the correct load function based on the graph format.
	 *
	 * @param graphFormat the graph format specification
	 * @return a function that can parse edges from the given input specification
	 */
	private def loadEdges(graphFormat : GraphFormat) : RDD[String] => RDD[(VertexId, VertexId)] = {
		(graphFormat.isEdgeBased(), graphFormat.isDirected()) match {
			case (false, _) => loadEdgesFromVertexData
			case (true, true) => loadEdgesFromEdgeDirectedData
			case (true, false) => loadEdgesFromEdgeUndirectedData
		}
	}

	/**
	 * @param graphData graph data in directed, edge-based format
	 * @return a set of parsed edges
	 */
	private def loadEdgesFromEdgeDirectedData(graphData : RDD[String]) : RDD[(VertexId, VertexId)] = {
		def lineToEdge(s : String) : (VertexId, VertexId) = {
			val tokens = s.trim.split("""\s""")
			(tokens(0).toLong, tokens(1).toLong)
		}
		
		graphData.map(lineToEdge)
	}

	/**
	 * @param graphData graph data in undirected, edge-based format
	 * @return a set of parsed edges
	 */
	private def loadEdgesFromEdgeUndirectedData(graphData : RDD[String]) : RDD[(VertexId, VertexId)] = {
		def lineToEdges(s : String) : Array[(VertexId, VertexId)] = {
			val tokens = s.trim.split("""\s""")
			Array((tokens(0).toLong, tokens(1).toLong), (tokens(1).toLong, tokens(0).toLong))
		}
		
		graphData.flatMap(lineToEdges)
	}

	/**
	 * @param graphData graph data in vertex-based format
	 * @return a set of parsed edges
	 */
	private def loadEdgesFromVertexData(graphData : RDD[String]) : RDD[(VertexId, VertexId)] = {
		def lineToEdges(s : String) : Array[(VertexId, VertexId)] = {
			val tokens = s.trim.split("""\s""")
			val src = tokens(0).toLong
			
			tokens.tail.map(s => (src, s.toLong))
		}
		
		graphData.flatMap(lineToEdges)
	}
	
}