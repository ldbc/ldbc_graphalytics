package nl.tudelft.graphalytics.graphx.cd

import nl.tudelft.graphalytics.domain.GraphFormat
import nl.tudelft.graphalytics.domain.algorithms.CommunityDetectionParameters
import nl.tudelft.graphalytics.graphx.AbstractJobTest
import org.apache.spark.graphx.Graph
import org.scalatest.FunSuite

import scala.io.Source

/**
  * Created by tim on 2/10/15.
  */
class CommunityDetectionJobTest extends FunSuite with AbstractJobTest {

	 test("Community detection on directed example graph") {
		 // Initialize the connected components job
		 val connJob = new CommunityDetectionJob(
			 "ignored",
			 new GraphFormat(true, false),
			 "ignored",
		    new CommunityDetectionParameters(0.1f, 0.1f, 5)
		 )

		 // Execute the conn. comp. job
		 performTest[VertexData, EdgeData](connJob, getClass().getResource("/test-examples/cd-dir-input"),
			 (result : Graph[VertexData, EdgeData]) => {
				 val resVertices = result.vertices.collect().toMap

				 // Parse the expected result
				 val expected = Source.fromURL(getClass().getResource("/test-examples/cd-dir-output")).getLines().map(
					 _.split(" ").map(_.toLong).toList).toList

				 // Verify the result
				 assertResult(expected.map(_.size).reduce(_ + _), "vertices in the result") {
					 resVertices.size
				 }

				 var observedCommunities = Set[Long]()
				 expected.foreach {
					 community => {
						 val communityLabel = resVertices.get(community(0)).get._3.get._1
						 assertResult(true, "vertex " + community(0) + " marks new community") {
							 !observedCommunities.contains(communityLabel)
						 }
						 observedCommunities = observedCommunities + communityLabel
						 community.foreach {
							 vid => assertResult(communityLabel, "community label of vertex " + vid) {
								 resVertices.get(vid).get._3.get._1
							 }
						 }
					 }
				 }
			 }
		 )
	 }

	test("Community detection on undirected example graph") {
		// Initialize the connected components job
		val connJob = new CommunityDetectionJob(
			"ignored",
			new GraphFormat(true, false),
			"ignored",
			new CommunityDetectionParameters(0.1f, 0.1f, 5)
		)

		// Execute the conn. comp. job
		performTest[VertexData, EdgeData](connJob, getClass().getResource("/test-examples/cd-undir-input"),
			(result : Graph[VertexData, EdgeData]) => {
				val resVertices = result.vertices.collect().toMap

				// Parse the expected result
				val expected = Source.fromURL(getClass().getResource("/test-examples/cd-undir-output")).getLines().map(
					_.split(" ").map(_.toLong).toList).toList

				// Verify the result
				assertResult(expected.map(_.size).reduce(_ + _), "vertices in the result") {
					resVertices.size
				}

				var observedCommunities = Set[Long]()
				expected.foreach {
					community => {
						val communityLabel = resVertices.get(community(0)).get._3.get._1
						assertResult(true, "vertex " + community(0) + " marks new community") {
							!observedCommunities.contains(communityLabel)
						}
						observedCommunities = observedCommunities + communityLabel
						community.foreach {
							vid => assertResult(communityLabel, "community label of vertex " + vid) {
								resVertices.get(vid).get._3.get._1
							}
						}
					}
				}
			}
		)
	}

 }
