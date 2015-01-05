package org.tudelft.graphalytics.graphx.evo

import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.tudelft.graphalytics.GraphFormat
import org.tudelft.graphalytics.algorithms.EVOParameters
import org.tudelft.graphalytics.graphx.GraphXJob

/**
 * The implementation of the graph evolution (forest fire model) algorithm on GraphX.
 *
 * @param graphPath the input path of the graph
 * @param graphFormat the format of the graph data
 * @param outputPath the output path of the computation
 * @author Tim Hegeman
 */
class ForestFireModelJob(graphPath : String, graphFormat : GraphFormat, outputPath : String,
                         parameters : Object)
		extends GraphXJob[Boolean, Int](graphPath, graphFormat, outputPath) {

	val evoParam : EVOParameters = parameters match {
		case p : EVOParameters => p
		case _ => null
	}

	/**
	 * Perform the graph computation using job-specific logic.
	 *
	 * @param graph the parsed graph with default vertex and edge values
	 * @return the resulting graph after the computation
	 */
	override def compute(graph: Graph[Boolean, Int]): Graph[Boolean, Int] = {
		// Create random vertices
		val newVerts = sparkContext.parallelize(
			(evoParam.getMaxId + 1 to evoParam.getMaxId + evoParam.getNumNewVertices).
					map(v => (graph.pickRandomVertex(), Set(v)))
		)
		var edgeList = graph.vertices.aggregateUsingIndex[Set[VertexId]](newVerts, _ ++ _).cache()
		var burningVerts = edgeList
		// Merge the source data into the graph
		var g = graph.outerJoinVertices(edgeList) {
			(_, _, sources) => sources.getOrElse(Set.empty[VertexId])
		}.cache()

		// Perform a number of iterations of the forest fire simulation
		var i = 0
		while (i < evoParam.getMaxIterations && burningVerts.count() > 0) {
			val burningVertGraph = g.outerJoinVertices(burningVerts) {
				(_, data, burningOpt) => (data, burningOpt.getOrElse(Set.empty[VertexId]))
			}.cache()

			// Select outgoing links and burn them
			val newOutLinks = {
				// Determine the candidate outgoing links for each burning vertex and source pair
				val eligibleOutLinks = burningVertGraph.mapReduceTriplets[Map[VertexId, Set[VertexId]]](
					// For each outgoing edge, determine which active sources have not
					// yet reached the destination vertex
					edge => (edge.srcAttr._2 &~ edge.dstAttr._1).map(
						source => (edge.srcId, Map(source -> Set(edge.dstId)))
					).toIterator,
					// Gather for each source the set of outgoing links to choose from
					(A, B) => (A.keySet ++ B.keySet).map(
						source => (source, (A.contains(source), B.contains(source)) match {
							case (false, false) => Set.empty[VertexId] // Impossible case
							case (true, false) => A.get(source).get
							case (false, true) => B.get(source).get
							case (true, true) => A.get(source).get ++ B.get(source).get
						})
					).toMap,
					// Limit the selection to burning vertices
					Some((burningVerts, EdgeDirection.Out))
				)
				// Select links to burn
				val newBurningVertsUngrouped = eligibleOutLinks.flatMap {
					case (_, options) => options.flatMap {
						case (src, outLinks) => Set.empty[(VertexId, Set[VertexId])]
						// TODO: Randomly select a geometric random number of links per source
					}
				}
				val newBurningVerts = g.vertices.aggregateUsingIndex[Set[VertexId]](
					newBurningVertsUngrouped, (A, B) => A ++ B
				).cache()

				// Merge the newly burning vertices into the graph
				val oldG = g
				g = g.outerJoinVertices(newBurningVerts) {
					(_, oldSources, newSources) =>
						if (newSources.isDefined) oldSources ++ newSources.get
						else oldSources
				}
				oldG.edges.unpersist(false)
				oldG.unpersistVertices(false)

				newBurningVerts
			}

			// Select incoming links and burn them
			val newInLinks = {
				// Determine the candidate incoming links for each burning vertex and source pair
				val eligibleInLinks = burningVertGraph.mapReduceTriplets[Map[VertexId, Set[VertexId]]](
					// For each incoming edge, determine which active sources have not
					// yet reached the destination vertex
					edge => (edge.dstAttr._2 &~ edge.srcAttr._1).map(
						source => (edge.dstId, Map(source -> Set(edge.srcId)))
					).toIterator,
					// Gather for each source the set of incoming links to choose from
					(A, B) => (A.keySet ++ B.keySet).map(
						source => (source, (A.contains(source), B.contains(source)) match {
							case (false, false) => Set.empty[VertexId] // Impossible case
							case (true, false) => A.get(source).get
							case (false, true) => B.get(source).get
							case (true, true) => A.get(source).get ++ B.get(source).get
						})
					).toMap,
					// Limit the selection to burning vertices
					Some((burningVerts, EdgeDirection.In))
				)
				// Select links to burn
				val newBurningVertsUngrouped = eligibleInLinks.flatMap {
					case (_, options) => options.flatMap {
						case (src, inLinks) => Set.empty[(VertexId, Set[VertexId])]
						// TODO: Randomly select a geometric random number of links per source
					}
				}
				val newBurningVerts = g.vertices.aggregateUsingIndex[Set[VertexId]](
					newBurningVertsUngrouped, (A, B) => A ++ B
				).cache()

				// Merge the newly burning vertices into the graph
				val oldG = g
				g = g.outerJoinVertices(newBurningVerts) {
					(_, oldSources, newSources) =>
						if (newSources.isDefined) oldSources ++ newSources.get
						else oldSources
				}
				oldG.edges.unpersist(false)
				oldG.unpersistVertices(false)

				newBurningVerts
			}

			// Clean up this iteration's intermediary data
			burningVertGraph.edges.unpersist(false)
			burningVertGraph.vertices.unpersist(false)

			// Update the burning vertex list
			val oldBurningVerts = burningVerts
			burningVerts = g.vertices.aggregateUsingIndex[Set[VertexId]](newOutLinks.union(newInLinks), (A, B) => A ++ B).cache()
			oldBurningVerts.unpersist(false)
			newOutLinks.unpersist(false)
			newInLinks.unpersist(false)

			// Update the final edge list
			val oldEdgeList = edgeList
			edgeList = g.vertices.aggregateUsingIndex[Set[VertexId]](edgeList.union(burningVerts), (A, B) => A ++ B).cache()
			oldEdgeList.unpersist()

			i = i + 1
		}

		// Merge the new edges into the original graph
		val graphEdges = g.edges.union(edgeList.flatMap {
			case (vid, sources) => sources.map(Edge(_, vid, 1))
		})
		Graph.fromEdges(graphEdges, false)
	}

	/**
	 * Convert a graph to the output format of this job.
	 *
	 * @return a RDD of strings (lines of output)
	 */
	override def makeOutput(graph: Graph[Boolean, Int]): RDD[String] = ???

	/**
	 * @return name of the GraphX job
	 */
	override def getAppName: String = "Forest Fire Model"

	/**
	 * @return true iff the input is valid
	 */
	override def hasValidInput: Boolean = evoParam != null
}
