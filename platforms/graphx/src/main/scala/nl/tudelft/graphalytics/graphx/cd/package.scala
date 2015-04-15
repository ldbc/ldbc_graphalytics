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

import org.apache.spark.graphx.VertexId

import scala.collection.mutable

/**
 * Types for the CommunityDetection GraphX job.
 * 
 * @author Sietse Au
 * @author Tim Hegeman
 */
package object cd {

	type Label = VertexId
	type Score = Double
	type Delta = Double
	type VertexDegree = Int
	type LabelToPropagate = (Label, Score, VertexDegree)
	type Neighbours = Set[Label]
	type VertexData = (Label, Neighbours, Option[LabelToPropagate])
	type EdgeData = Int
	type MessageData = Array[LabelToPropagate]
	
}