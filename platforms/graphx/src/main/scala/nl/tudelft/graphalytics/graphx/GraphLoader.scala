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
			defaultValue : VD) : Graph[VD, Int] = {
		val graph = Graph.fromEdgeTuples(loadEdges(graphFormat)(graphData), defaultValue)
		if (!graphFormat.isEdgeBased) {
			val vertices = loadVerticesFromVertexData(graphData, defaultValue)
			Graph(graph.vertices.union(vertices).distinct(), graph.edges, defaultValue)
		} else {
			graph
		}
	}

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

	/**
	 * @param graphData graph data in vertex-based format
	 * @param defaultValue default value of vertices
	 * @tparam VD vertex value type
	 * @return a set of vertices that were potentially not included in the edge set
	 */
	private def loadVerticesFromVertexData[VD : ClassTag](graphData : RDD[String], defaultValue : VD) :
			RDD[(VertexId, VD)] = {
		def lineToVertex(s : String) : Iterable[(VertexId, VD)] = {
			val tokens = s.trim.split("""\s""")
			val src = tokens(0).toLong
			if (tokens.size == 1)
				Iterable((src, defaultValue))
			else
				Iterable()
		}

		graphData.flatMap(lineToVertex)
	}
	
}