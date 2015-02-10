package nl.tudelft.graphalytics.graphx.conn

import nl.tudelft.graphalytics.domain.GraphFormat
import nl.tudelft.graphalytics.graphx.AbstractJobTest
import org.apache.spark.graphx.Graph
import org.scalatest.FunSuite

import scala.io.Source

/**
 * Integration test for connected components on GraphX.
 *
 * @author Tim Hegeman
 */
class ConnectedComponentsJobTest extends FunSuite with AbstractJobTest {

	test("Connected components on directed example graph") {
		// Initialize the connected components job
		val connJob = new ConnectedComponentsJob(
			"ignored",
			new GraphFormat(true, false),
			"ignored"
		)

		// Execute the conn. comp. job
		performTest[Long, Int](connJob, getClass().getResource("/test-examples/conn-input"),
			(result : Graph[Long, Int]) => {
				val resVertices = result.vertices.collect().toMap

				// Parse the expected result
				val expected = readGraphWithValues(getClass().getResource("/test-examples/conn-output"), {
					_(0).toLong
				})

				// Verify the result
				assertResult(expected.size, "vertices in the result") {
					resVertices.size
				}
				expected.foreach {
					case (vid, component) => assertResult(component, "component of vertex " + vid) {
						resVertices.get(vid).get
					}
				}
			}
		)
	}

}
