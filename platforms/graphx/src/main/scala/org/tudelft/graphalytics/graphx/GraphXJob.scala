package org.tudelft.graphalytics.graphx

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import scala.reflect.ClassTag
import org.tudelft.graphalytics.GraphFormat
import org.apache.spark.graphx.{Graph, VertexId}

/**
 * Base class for all GraphX jobs in the Graphalytics benchmark. Handles the Spark
 * setup, graph loading, and writing back results.
 */
abstract class GraphXJob[VD : ClassTag](graphPath : String, graphFormat : GraphFormat,
		outputPath : String) extends Serializable {

	// Set up the Spark context for use in the GraphX job.
	@transient val sparkConfiguration = new SparkConf()
	sparkConfiguration.setAppName(s"Graphalytics: ${getAppName}")
	sparkConfiguration.setMaster("yarn-client")
	@transient val sparkContext = new SparkContext(sparkConfiguration)

	/**
	 * Executes the full GraphX job by reading and parsing the input graph,
	 * running the job-specific graph computation, and writing back the result. 
	 */
	def runJob = {
		// Load the raw graph data
		val graphData : RDD[String] = sparkContext.textFile(graphPath)
		// Parse the vertex and edge data
		val graph = GraphLoader.loadGraph(graphData, graphFormat, false)
				.mapVertices((vid, _) => getInitialValue(vid)).cache()

		// Run the graph computation
		val output = compute(graph)
		
		// Output graph in job-specific format
		makeOutput(output).saveAsTextFile(outputPath)
	}
	
	/**
	 * Perform the graph computation using job-specific logic.
	 * 
	 * @return the resulting graph after the computation
	 */
	def compute(graph : Graph[VD, Int]) : Graph[VD, Int]
	
	/**
	 * Convert a graph to the output format of this job.
	 * 
	 * @return a RDD of strings (lines of output)
	 */
	def makeOutput(graph : Graph[VD, Int]) : RDD[String]
	
	/**
	 * @return true iff the input is valid
	 */
	def hasValidInput : Boolean
	
	/**
	 * @return name of the GraphX job
	 */
	def getAppName : String
	
	/**
	 * @param vertexId ID of a vertex
	 * @return the initial value corresponding with vertexID
	 */
	def getInitialValue(vertexId : VertexId) : VD
	
}