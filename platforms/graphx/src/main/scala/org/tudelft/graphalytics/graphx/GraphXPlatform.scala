package org.tudelft.graphalytics.graphx

import org.tudelft.graphalytics.Platform
import org.tudelft.graphalytics.Graph
import org.tudelft.graphalytics.algorithms.AlgorithmType
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.tudelft.graphalytics.graphx.bfs.BreadthFirstSearchJob
import org.tudelft.graphalytics.graphx.cd.CommunityDetectionJob
import org.tudelft.graphalytics.graphx.conn.ConnectedComponentsJob
import org.tudelft.graphalytics.graphx.evo.ForestFireModelJob
import org.tudelft.graphalytics.graphx.stats.LocalClusteringCoefficientJob

/**
 * Constants for GraphXPlatform
 */
object GraphXPlatform {
	val HDFS_PATH = "graphalytics-graphx"

	val CONFIG_PATH = "graphx.properties"
	val CONFIG_JOB_NUM_EXECUTORS = "graphx.job.num-executors"
	val CONFIG_JOB_EXECUTOR_MEMORY = "graphx.job.executor-memory"
	val CONFIG_JOB_EXECUTOR_CORES = "graphx.job.executor-cores"
}

/**
 * Graphalytics Platform implementation for GraphX. Manages the datasets on HDFS and launches the appropriate
 * GraphX jobs.
 */
class GraphXPlatform extends Platform {
	import GraphXPlatform._

	var pathsOfGraphs : Map[String, String] = Map()

	/* Parse the GraphX configuration file */
	val config = Properties.fromFile(CONFIG_PATH).getOrElse(Properties.empty())
	System.setProperty("spark.executor.cores", config.getString(CONFIG_JOB_EXECUTOR_CORES).getOrElse("1"))
	System.setProperty("spark.executor.memory", config.getString(CONFIG_JOB_EXECUTOR_MEMORY).getOrElse("2g"))
	System.setProperty("spark.executor.instances", config.getString(CONFIG_JOB_NUM_EXECUTORS).getOrElse("1"))

	/**
	 * @inheritdoc
	 */
	def uploadGraph(graph : Graph, filePath : String) = {
		val localPath = new Path(filePath)
		val hdfsPath = new Path(s"$HDFS_PATH/input/${graph.getName}")

		val fs = FileSystem.get(new Configuration())
		fs.copyFromLocalFile(localPath, hdfsPath)
		fs.close()
		
		pathsOfGraphs += (graph.getName -> hdfsPath.toUri.getPath)
	}

	/**
	 * @inheritdoc
	 */
	def executeAlgorithmOnGraph(algorithmType : AlgorithmType,
			graph : Graph, parameters : Object) : Boolean = {
		try  {
			val path = pathsOfGraphs(graph.getName)
			val outPath = s"$HDFS_PATH/output/${graph.getName}-${algorithmType.name}"
			val format = graph.getGraphFormat
			
			val job = algorithmType match {
				case AlgorithmType.BFS => new BreadthFirstSearchJob(path, format, outPath, parameters)
				case AlgorithmType.CD => new CommunityDetectionJob(path, format, outPath, parameters)
				case AlgorithmType.CONN => new ConnectedComponentsJob(path, format, outPath)
				case AlgorithmType.EVO => new ForestFireModelJob(path, format, outPath, parameters)
				case AlgorithmType.STATS => new LocalClusteringCoefficientJob(path, format, outPath)
				case x => {
					System.err.println(s"Invalid algorithm type: $x")
					return false
				}
			}
			
			if (job.hasValidInput) {
				job.runJob
				true
			} else {
				false
			}
		} catch {
			case e : Exception => e.printStackTrace(); false
		}
	}

	/**
	 * @inheritdoc
	 */
	def deleteGraph(graphName : String) = {
		// Not implemented
	}

	/**
	 * @inheritdoc
	 */
	def getName() : String = "graphx"

}
