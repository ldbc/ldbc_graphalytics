package nl.tudelft.graphalytics.graphx.stats

import nl.tudelft.graphalytics.domain.GraphFormat
import nl.tudelft.graphalytics.graphx.AbstractJobTest
import org.apache.spark.graphx.Graph
import org.scalatest.FunSuite

import scala.io.Source

/**
 * Integration test for local clustering coefficient on GraphX.
 *
 * @author Tim Hegeman
 */
class LocalClusteringCoefficientJobTest extends FunSuite with AbstractJobTest {

	val ERROR_BOUND = 1e-4

	test("Local clustering coefficient on directed example graph") {
		// Initialize the local clustering coefficient job
		val lccJob = new LocalClusteringCoefficientJob(
			"ignored",
			new GraphFormat(true, false),
			"ignored"
		)

		// Execute the LCC job
		performTest[Double, Int](lccJob, getClass().getResource("/test-examples/stats-dir-input"),
			(result : Graph[Double, Int]) => {
				val resVertices = result.vertices.collect().toMap

				// Parse the expected result
				val expectedFileLines = Source.fromURL(getClass().getResource("/test-examples/stats-dir-output")).
						getLines().toList
				val expectedMeanLcc = expectedFileLines(0).toDouble
				val expected = expectedFileLines.drop(1).map { s => {
					val tokens = s.split(" ")
					(tokens(0).toLong, tokens(1).toDouble)
				}}.toMap

				// Verify the result
				assertResult(expected.size, "vertices in the result") {
					resVertices.size
				}
				expected.foreach {
					case (vid, lcc) => {
						val actualLcc = resVertices.get(vid).get
						if (actualLcc < lcc - ERROR_BOUND || actualLcc > lcc + ERROR_BOUND) {
							assertResult(lcc, "local clustering coefficient of vertex " + vid) {
								actualLcc
							}
						}
					}
				}

				val meanLcc = lccJob.makeOutput(result).meanLcc
				if (meanLcc < expectedMeanLcc - ERROR_BOUND || meanLcc > expectedMeanLcc + ERROR_BOUND) {
					assertResult(expectedMeanLcc, "mean local clustering coefficient") {
						meanLcc
					}
				}
			}
		)
	}

	test("Local clustering coefficient on undirected example graph") {
		// Initialize the local clustering coefficient job
		val lccJob = new LocalClusteringCoefficientJob(
			"ignored",
			new GraphFormat(false, false),
			"ignored"
		)

		// Execute the LCC job
		performTest[Double, Int](lccJob, getClass().getResource("/test-examples/stats-undir-input"),
			(result : Graph[Double, Int]) => {
				val resVertices = result.vertices.collect().toMap

				// Parse the expected result
				val expectedFileLines = Source.fromURL(getClass().getResource("/test-examples/stats-undir-output")).
						getLines().toList
				val expectedMeanLcc = expectedFileLines(0).toDouble
				val expected = expectedFileLines.drop(1).map { s => {
					val tokens = s.split(" ")
					(tokens(0).toLong, tokens(1).toDouble)
				}}.toMap

				// Verify the result
				assertResult(expected.size, "vertices in the result") {
					resVertices.size
				}
				expected.foreach {
					case (vid, lcc) => {
						val actualLcc = resVertices.get(vid).get
						if (actualLcc < lcc - ERROR_BOUND || actualLcc > lcc + ERROR_BOUND) {
							assertResult(lcc, "local clustering coefficient of vertex " + vid) {
								actualLcc
							}
						}
					}
				}

				val meanLcc = lccJob.makeOutput(result).meanLcc
				if (meanLcc < expectedMeanLcc - ERROR_BOUND || meanLcc > expectedMeanLcc + ERROR_BOUND) {
					assertResult(expectedMeanLcc, "mean local clustering coefficient") {
						meanLcc
					}
				}
			}
		)
	}

}