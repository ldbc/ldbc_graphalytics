package org.tudelft.graphalytics.graphx

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
	type MessageData = mutable.Map[Label, LabelToPropagate]
	
}