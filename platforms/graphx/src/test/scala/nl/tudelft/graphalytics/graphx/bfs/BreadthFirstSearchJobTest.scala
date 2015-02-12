package nl.tudelft.graphalytics.graphx.bfs

import nl.tudelft.graphalytics.domain.GraphFormat
import nl.tudelft.graphalytics.domain.algorithms.BreadthFirstSearchParameters
import nl.tudelft.graphalytics.graphx.AbstractJobTest
import org.apache.spark.graphx.Graph
import org.scalatest.FunSuite

/**
 * Integration test for BFS job on GraphX.
 *
 * @author Tim Hegeman
 */
class BreadthFirstSearchJobTest extends FunSuite with AbstractJobTest {

	test("BFS on example graph") {
		// Initialize the BFS job
		val bfsJob = new BreadthFirstSearchJob(
			"ignored",
			new GraphFormat(true, false),
			"ignored",
			new BreadthFirstSearchParameters(1L)
		)

		// Execute the BFS job
		performTest[Long, Int](bfsJob, getClass().getResource("/test-examples/bfs-input"),
			(result: Graph[Long, Int]) => {
				val resVertices = result.vertices.collect().toMap

				// Parse the expected result
				val expected = readGraphWithValues(getClass().getResource("/test-examples/bfs-output"), {
					_(0).toLong
				})

				// Verify the result
				assertResult(expected.size, "vertices in the result") {
					resVertices.size
				}
				expected.foreach {
					case (vid, distance) => assertResult(distance, "distance of vertex " + vid) {
						resVertices.get(vid).get
					}
				}
			}
		)
	}

}
