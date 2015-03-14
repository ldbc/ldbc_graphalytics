package nl.tudelft.graphalytics.graphx.cd

import nl.tudelft.graphalytics.domain.GraphFormat
import nl.tudelft.graphalytics.domain.algorithms.CommunityDetectionParameters
import nl.tudelft.graphalytics.graphx.{GraphXJobOutput, GraphXPregelJob}
import org.apache.spark.graphx.{EdgeDirection, EdgeTriplet, Graph, VertexId}
import scala.collection.JavaConversions._

import scala.collection.mutable

/**
 * The implementation of the community detection algorithm on GraphX.
 *
 * @param graphPath the input path of the graph
 * @param graphFormat the format of the graph data
 * @param outputPath the output path of the computation
 * @param parameters the graph-specific parameters for community detection
 * @author Sietse Au
 * @author Tim Hegeman
 */
class CommunityDetectionJob(graphPath : String, graphFormat : GraphFormat,
		outputPath : String, parameters : Object)
		extends	GraphXPregelJob[VertexData, EdgeData, MessageData](graphPath, graphFormat, outputPath) {

	val cdParam : CommunityDetectionParameters = parameters match {
		case p : CommunityDetectionParameters => p
		case _ => null
	}

	/**
	 * Preprocess the parsed graph (with default vertex and edge values) to a
	 * graph with correct initial values.
	 *
	 * @param graph input graph
	 * @return preprocessed graph
	 */
	override def preprocess(graph: Graph[Boolean, Int]): Graph[VertexData, EdgeData] =
		graph.outerJoinVertices(graph.collectNeighborIds(EdgeDirection.Either).mapValues(x => x.toSet)) {
			(vid, _, neighbours) => (vid, neighbours.getOrElse(Set.empty), None)
		}

	/**
	 * @return initial message to send to all vertices
	 */
	override def getInitialMessage: MessageData = Array.empty

	/**
	 * Pregel messasge combiner. Merges two messages for the same vertex to a
	 * single message.
	 *
	 * @return the aggregated message
	 */
	override def mergeMsg = (A : MessageData, B : MessageData) => A ++ B

	/**
	 * Pregel vertex program. Computes a new vertex value based for a given
	 * vertex ID, the old value of the vertex, and aggregated messages.
	 *
	 * @return the new value of the vertex
	 */
	override def vertexProgram = (vid : VertexId, vertexData: VertexData, messageData: MessageData) =>
		(vertexData._1, vertexData._2, Some(determineLabel(vertexData, messageData)))

	/**
	 * Pregel message generation. Produces for each edge a set of messages.
	 *
	 * @return a set of messages to send
	 */
	override def sendMsg = (edge : EdgeTriplet[VertexData, EdgeData]) => {
		val messageData = Array(edge.srcAttr._3.get)
		val messageData2 = Array(edge.dstAttr._3.get)
		Iterator((edge.dstId, messageData), (edge.srcId, messageData2))
	}

	/**
	 * Convert a graph to the output format of this job.
	 *
	 * @return a GraphXJobOutput object representing the job result
	 */
	override def makeOutput(graph: Graph[VertexData, EdgeData]) =
		new GraphXJobOutput(graph.vertices.map(vertex => {
			val labelData = vertex._2._3.get
			s"${vertex._1} ${labelData._1}:${labelData._2}"
		}).cache())

	/**
	 * @return name of the GraphX job
	 */
	override def getAppName: String = "Community Detection"

	/**
	 * @return true iff the input is valid
	 */
	override def hasValidInput: Boolean = (cdParam != null)

	/**
	 * @return the maximum number of iterations to run the Pregel algorithm for.
	 */
	override def getMaxIterations: Int = cdParam.getMaxIterations

	def weightedScore(labelScore : Score, arbFuncValue : Score, weightOfEdge : Int = 1) : Score = {
		(labelScore * math.pow(arbFuncValue, cdParam.getNodePreference) * weightOfEdge)
	}

	def determineLabel(vertexData : VertexData, receivedMessage : MessageData) : LabelToPropagate = {
		vertexData._3 match {
			case None => (vertexData._1, 1.0, vertexData._2.size)
			case _ =>
				val aggregatedLabelScores : mutable.Map[Label, (Score, VertexDegree)] = new java.util.HashMap[Label, (Score, VertexDegree)]()
				val maxLabelScores : mutable.Map[Label, (Score, VertexDegree)] = new java.util.HashMap[Label, (Score, VertexDegree)]()

				receivedMessage.foreach {
					case (label, score, degree) =>						val weightedScore1 = weightedScore(score, degree)
						aggregatedLabelScores.get(label) match {
							case None => {
								aggregatedLabelScores += (label -> (weightedScore1, degree))
								maxLabelScores += (label -> (score, degree))
							}
							case Some (retrievedScore) => {
								aggregatedLabelScores += (label -> (aggregatedLabelScores(label)._1 + weightedScore1, degree))
								if (maxLabelScores(label)._1 < score)
									maxLabelScores += (label -> (score, degree))
							}
						}
				}

				//          println("AGG LABEL SCORE: " +  aggregatedLabelScores)
				//          println("MAX LABEL SCORE: " + maxLabelScores)
				val (chosenLabel, (_, _)) = aggregatedLabelScores.reduce{(a,b) =>
					val (_, (score1, _)) = a
					val (_, (score2, _)) = b
					if (math.abs(score1 - score2) < 1e-4) {
						///break ties by choosing smallest.
						if (a._1 < b._1) {
							a
						} else {
							b
						}
					} else if (score2 > score1) {
						b
					} else {
						a
					}
				}

				val oldLabel = vertexData._3.get._1
				val attenuatedChosenLabelScore = if (chosenLabel == oldLabel) maxLabelScores(chosenLabel)._1 else maxLabelScores(chosenLabel)._1 - cdParam.getHopAttenuation

				(chosenLabel, attenuatedChosenLabelScore, vertexData._2.size)
		}
	}

}