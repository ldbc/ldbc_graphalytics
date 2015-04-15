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
package nl.tudelft.graphalytics.graphx.evo

import nl.tudelft.graphalytics.domain.GraphFormat
import nl.tudelft.graphalytics.domain.algorithms.ForestFireModelParameters
import nl.tudelft.graphalytics.graphx.AbstractJobTest
import org.apache.spark.graphx.Graph
import org.scalatest.FunSuite

/**
 * Integration tests for forest fire model implementation on GraphX.
 *
 * @author Tim Hegeman
 */
class ForestFireModelJobTest extends FunSuite with AbstractJobTest {

	val P_RATIO = 0.5f
	val R_RATIO = 0.5f
	val MAX_ID = 50L
	val MAX_ITERATIONS = 2
	val NEW_VERTICES = 5

	def verifyOldGraphUnchanged(oldGraph : Map[Long, Set[Long]], newGraph : Graph[Boolean, Int]) = {
		val oldVertices = oldGraph.keySet
		val newVertices = newGraph.vertices.collect().map(_._1)
		assertResult(true, "original vertices are in new graph") {
			oldVertices.subsetOf(newVertices.toSet)
		}

		val oldEdges = oldGraph.toArray.flatMap { case (v, es) => es.map(e => (v, e)) }.toSet
		val newEdges = newGraph.edges.collect().map(e => (e.srcId, e.dstId)).toSet
		assertResult(true, "original edges are in new graph") {
			oldEdges.subsetOf(newEdges)
		}

		val addedEdges = newEdges.diff(oldEdges)
		assertResult(Set[(Long, Long)](), "no new edges added starting from original vertices") {
			addedEdges.filter { case (src, dst) => src <= MAX_ID }
		}
	}

	def verifyNewVerticesCreated(newGraph : Graph[Boolean, Int]) = {
		val vertices = newGraph.vertices.collect().map(_._1)
		assertResult(MAX_ID + NEW_VERTICES, "vertices in new graph") {
			vertices.size
		}

		(MAX_ID + 1 until MAX_ID + NEW_VERTICES).foreach {
			vid => assertResult(true, "vertex " + vid + " created in new graph") {
				vertices.contains(vid)
			}
		}
	}

	def verifyNewEdgesFeasible(newGraph : Graph[Boolean, Int]) = {
		(MAX_ID + 1 until MAX_ID + NEW_VERTICES).foreach {
			newVertex => {
				val vertices = newGraph.edges.filter(_.srcId == newVertex).collect().map(_.dstId)
				val subgraph = newGraph.subgraph(vpred = (v, d) => vertices.contains(v))
				val edges = subgraph.edges.union(subgraph.edges.reverse).collect().map(e => (e.srcId, e.dstId)).toSet
				val edgeSets = edges.groupBy(_._1).mapValues(_.map(_._2))

				def bfs(visited : Set[Long], front : Set[Long], depth : Int) : Set[Long] = {
					if (depth == 0) {
						visited
					} else {
						val reachable = front.flatMap(f => edgeSets.getOrElse(f, Set()))
						bfs(visited ++ reachable, reachable -- visited, depth - 1)
					}
				}

				assertResult(true, "edges for vertex " + newVertex + " are feasible") {
					vertices.exists {
						v => bfs(Set(v), Set(v), MAX_ITERATIONS).size == vertices.size
					}
				}
			}
		}
	}

	test("Forest fire model on directed example graph") {
		// Initialize the forest fire model job
		val ffmJob = new ForestFireModelJob(
			"ignored",
			new GraphFormat(true, false),
			"ignored",
			new ForestFireModelParameters(MAX_ID, P_RATIO, R_RATIO, MAX_ITERATIONS, NEW_VERTICES)
		)

		// Execute the forest fire model job
		performTest[Boolean, Int](ffmJob, getClass().getResource("/test-examples/evo-dir-input"),
			(result : Graph[Boolean, Int]) => {
				// Parse the original graph
				val original = readGraphStructure(getClass().getResource("/test-examples/evo-dir-input"))

				// Verify the result
				verifyOldGraphUnchanged(original, result)
				verifyNewVerticesCreated(result)
				verifyNewEdgesFeasible(result)
			}
		)
	}

	test("Forest fire model on undirected example graph") {
		// Initialize the forest fire model job
		val ffmJob = new ForestFireModelJob(
			"ignored",
			new GraphFormat(false, false),
			"ignored",
			new ForestFireModelParameters(MAX_ID, P_RATIO, R_RATIO, MAX_ITERATIONS, NEW_VERTICES)
		)

		// Execute the forest fire model job
		performTest[Boolean, Int](ffmJob, getClass().getResource("/test-examples/evo-undir-input"),
			(result : Graph[Boolean, Int]) => {
				// Parse the original graph
				val original = readGraphStructure(getClass().getResource("/test-examples/evo-undir-input"))

				// Verify the result
				verifyOldGraphUnchanged(original, result)
				verifyNewVerticesCreated(result)
				verifyNewEdgesFeasible(result)
			}
		)
	}

}
