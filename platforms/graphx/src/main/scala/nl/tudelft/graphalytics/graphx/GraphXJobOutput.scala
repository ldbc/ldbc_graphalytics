package nl.tudelft.graphalytics.graphx

import org.apache.spark.rdd.RDD

/**
 * Output data of a GraphX job. Can be written to a (distributed) filesystem, or kept in memory.
 *
 * @param graph string representation of the output graph
 * @author Tim Hegeman
 */
class GraphXJobOutput(val graph: RDD[String]) {

	/**
	 * Write the output of the GraphX job to the given path.
	 *
	 * @param path output path for the job
	 */
	def writeToPath(path: String) = {
		graph.saveAsTextFile(path)
	}

	/**
	 * Clean up the job output.
	 */
	def cleanUp(): Unit = {
		graph.unpersist(blocking = false)
	}

}
