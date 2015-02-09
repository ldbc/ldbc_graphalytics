package nl.tudelft.graphalytics.graphx.stats

import nl.tudelft.graphalytics.graphx.GraphXJobOutput
import org.apache.spark.rdd.RDD

/**
 * Output data of the local clustering coefficient job on GraphX.
 *
 * @param graph string representation of the output graph
 * @param meanLcc mean local clustering coefficient of the graph
 */
class LocalClusteringCoefficientJobOutput(graph : RDD[String], val meanLcc : Double)
		extends GraphXJobOutput(graph) {

	override def writeToPath(path : String) = {
		super.writeToPath(path + "/graph")
		graph.context.parallelize(Array(meanLcc.toString)).saveAsTextFile(path + "/lcc")
	}

}