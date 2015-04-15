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

import nl.tudelft.graphalytics.domain.GraphFormat
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import scala.reflect.ClassTag
import org.apache.spark.graphx.Graph

/**
 * Base class for all GraphX jobs in the Graphalytics benchmark. Handles the Spark
 * setup, graph loading, and writing back results.
 *
 * @tparam VD vertex data type
 * @tparam ED edge data type
 * @param graphPath the input path of the graph
 * @param graphFormat the format of the graph data
 * @param outputPath the output path of the computation
 */
abstract class GraphXJob[VD : ClassTag, ED : ClassTag](graphPath : String, graphFormat : GraphFormat,
		outputPath : String) extends Serializable {

	/**
	 * Executes the full GraphX job by reading and parsing the input graph,
	 * running the job-specific graph computation, and writing back the result. 
	 */
	def runJob() = {
		// Set up the Spark context for use in the GraphX job.
		val sparkConfiguration = new SparkConf()
		sparkConfiguration.setAppName(s"Graphalytics: ${getAppName}")
		sparkConfiguration.setMaster("yarn-client")
		sparkConfiguration.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
		val sparkContext = new SparkContext(sparkConfiguration)

		// Load the raw graph data
		val graphData : RDD[String] = sparkContext.textFile(graphPath)
		// Execute the job
		val result = executeOnGraph(graphData)
		// Create the output
		val output = makeOutput(result)
		// Write the result
		output.writeToPath(outputPath)

		// Clean up
		result.unpersistVertices(blocking = false)
		result.edges.unpersist(blocking = false)
		output.cleanUp()
		sparkContext.stop()
	}

	/**
	 * Executes the GraphX job using a given graph (as a Spark RDD) and returns the result of the job.
	 *
	 * @param graphData the input graph as a Spark RDD
	 * @return the output of the job
	 */
	def executeOnGraph(graphData : RDD[String]) : Graph[VD, ED] = {
		// Parse the vertex and edge data
		val graph = GraphLoader.loadGraph(graphData, graphFormat, false).cache()

		// Run the graph computation
		val output = compute(graph).cache()
		// Materialize the output and clean up the original graph
		output.vertices.count()
		output.edges.count()
		graph.unpersistVertices(blocking = false)
		graph.edges.unpersist(blocking = false)

		output
	}
	
	/**
	 * Perform the graph computation using job-specific logic.
	 * 
	 * @param graph the parsed graph with default vertex and edge values
	 * @return the resulting graph after the computation
	 */
	def compute(graph : Graph[Boolean, Int]) : Graph[VD, ED]
	
	/**
	 * Convert a graph to the output format of this job.
	 *
	 * @param graph the graph to output
	 * @return a GraphXJobOutput object representing the job result
	 */
	def makeOutput(graph : Graph[VD, ED]) : GraphXJobOutput
	
	/**
	 * @return true iff the input is valid
	 */
	def hasValidInput : Boolean
	
	/**
	 * @return name of the GraphX job
	 */
	def getAppName : String
	
}
