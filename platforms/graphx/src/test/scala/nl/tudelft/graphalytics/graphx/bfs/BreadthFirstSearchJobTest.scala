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
