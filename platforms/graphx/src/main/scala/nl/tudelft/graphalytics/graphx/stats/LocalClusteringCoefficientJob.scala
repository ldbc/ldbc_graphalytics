package nl.tudelft.graphalytics.graphx.stats

import nl.tudelft.graphalytics.domain.GraphFormat
import org.apache.spark.graphx._
import nl.tudelft.graphalytics.graphx.GraphXJob

/**
 * The implementation of the stats (LCC) algorithm on GraphX. Inspired by the TriangleCount implementation bundled
 * with GraphX.
 *
 * @param graphPath the input path of the graph
 * @param graphFormat the format of the graph data
 * @param outputPath the output path of the computation
 * @author Tim Hegeman
 */
class LocalClusteringCoefficientJob(graphPath : String, graphFormat : GraphFormat, outputPath : String)
	extends GraphXJob[Double, Int](graphPath, graphFormat, outputPath) {

	/**
	 * Computes the local clustering coefficient (LCC) for each vertex in the graph.
	 *
	 * @param graph the parsed graph with default vertex and edge values
	 * @return the resulting graph after the computation
	 */
	override def compute(graph: Graph[Boolean, Int]): Graph[Double, Int] = {
		// Deduplicate the edges to ensure that every pair of connected vertices of compared exactly once
		val canonicalGraph = Graph(graph.vertices, graph.edges.map(e =>
			if (e.srcId > e.dstId) Edge(e.dstId, e.srcId, 0)
			else Edge(e.srcId, e.dstId, 0)
		).distinct()).cache()

		// Collect for each vertex a list of neighbours, having a vertex id occur twice if edges exist in both directions
		val neighbours = graph.mapReduceTriplets[List[VertexId]](
			e => Iterator((e.srcId, List(e.dstId)), (e.dstId, List(e.srcId))),
			_ ++ _
		)

		// Merge the neighbour lists back into the canonical graph
		val neighbourGraph = canonicalGraph.outerJoinVertices[List[VertexId], List[VertexId]](neighbours) {
			(_, _, n) => n.map(_.sorted).getOrElse(List.empty[VertexId])
		}

		// Function for computing the number of vertices in set, that also occur in selection
		val overlap = (set : List[Long], selection : List[Long]) => {
			var setLeft = set
			var selectionLeft = selection
			var sum = 0L
			while (!setLeft.isEmpty && !selectionLeft.isEmpty) {
				if (setLeft.head < selectionLeft.head) {
					setLeft = setLeft.tail
				} else if (setLeft.head > selectionLeft.head) {
					selectionLeft = selectionLeft.tail
				} else {
					setLeft = setLeft.tail
					sum = sum + 1
				}
			}
			sum
		}

		// Counts for every vertex the number of edges in its neighbourhood
		val neighbourhoodEdges = neighbourGraph.mapReduceTriplets[Long](
			e => {
				// A vertex v has a list of vertices that defines its neighbourhood. v receives from all of its
				// neighbours the number of edges to other vertices in v's neighbourhood, counted in canonical direction
				val overlapToSrc = overlap(e.dstAttr, e.srcAttr.filter(_ > e.dstId))
				val overlapToDst = overlap(e.srcAttr, e.dstAttr.filter(_ > e.srcId))
				Iterator((e.srcId, overlapToSrc), (e.dstId, overlapToDst))
			},
			(a, b) => a + b
		)

		// Compute for every vertex the maximum number of edges in its neighbourhood
		val maxNeighbourhoodEdges = canonicalGraph
				.mapReduceTriplets[Long](e => Iterator((e.srcId, 1L), (e.dstId, 1L)), _ + _)
				.mapValues(num => num * (num - 1))

		// Compute the LCC of each vertex
		val result = graph.outerJoinVertices[Long, Long](maxNeighbourhoodEdges) {
			(_, _, value) => value.getOrElse(0L)
		}.outerJoinVertices(neighbourhoodEdges) {
			(_, max, value) => if (max == 0L) 0.0 else value.getOrElse(0L).toDouble / max
		}.cache()

		// Materialize the result
		result.vertices.count()
		result.edges.count()

		// Unpersist the canonical graph
		canonicalGraph.unpersistVertices(blocking = false)
		canonicalGraph.edges.unpersist(blocking = false)

		result
	}

	/**
	 * Outputs the mean local clustering coefficient of the graph.
	 *
	 * @param graph the graph to output
	 * @return a GraphXJobOutput object representing the job result
	 */
	override def makeOutput(graph: Graph[Double, Int]) = {
		val aggregatedLcc = graph.vertices.map[(Double, Long)](v => (v._2, 1L)).reduce(
			(A: (Double, Long), B: (Double, Long)) =>
				(A._1 + B._1, A._2 + B._2)
		)

		new LocalClusteringCoefficientJobOutput(
			graph.vertices.map(vertex => s"${vertex._1} ${vertex._2}").cache(),
			aggregatedLcc._1 / aggregatedLcc._2
		)
	}

	/**
	 * @return name of the GraphX job
	 */
	override def getAppName: String = "Local Clustering Coefficient"

	/**
	 * @return true iff the input is valid
	 */
	override def hasValidInput: Boolean = true

}
