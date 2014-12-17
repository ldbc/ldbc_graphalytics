package org.tudelft.graphalytics.graphx

import org.tudelft.graphalytics.Platform
import org.tudelft.graphalytics.Graph
import org.tudelft.graphalytics.algorithms.AlgorithmType
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.tudelft.graphalytics.graphx.bfs.BFSJob
import org.tudelft.graphalytics.algorithms.BFSParameters

object GraphXPlatform {
	val HDFS_PATH = "graphalytics-graphx"
}

class GraphXPlatform extends Platform {
	import GraphXPlatform._
	
	var pathsOfGraphs : Map[String, String] = Map()
	
	def uploadGraph(graph : Graph, filePath : String) = {
		val localPath = new Path(filePath)
		val hdfsPath = new Path(s"$HDFS_PATH/input/${graph.getName}")
		
		val fs = FileSystem.get(new Configuration())
		fs.copyFromLocalFile(localPath, hdfsPath)
		fs.close
		
		pathsOfGraphs += (graph.getName -> hdfsPath.getName)
	}
	
	/**
	 * SparkSubmit output:
		Main class:
		org.apache.spark.deploy.yarn.Client
		Arguments:
		--name
		org.apache.spark.examples.SparkPi
		--driver-memory
		4g
		--num-executors
		3
		--executor-memory
		2g
		--executor-cores
		1
		--jar
		file:/tmp/spark-1.1.1-bin-hadoop2.4/lib/spark-examples-1.1.1-hadoop2.4.0.jar
		--class
		org.apache.spark.examples.SparkPi
		--arg
		10
		System properties:
		spark.executor.memory -> 2g
		SPARK_SUBMIT -> true
		spark.app.name -> org.apache.spark.examples.SparkPi
		spark.master -> yarn-cluster
		Classpath elements:
	 */

	def executeAlgorithmOnGraph(algorithmType : AlgorithmType,
			graph : Graph, parameters : Object) : Boolean = {
		try  {
			val path = pathsOfGraphs(graph.getName)
			val format = graph.getGraphFormat
			
			val job = algorithmType match {
				case AlgorithmType.BFS => new BFSJob(path, format, parameters)
				case x => {
					System.err.println(s"Invalid algorithm type: $x")
					return false
				}
			}
			
			job.runJob
			println("Ran job")
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

	def deleteGraph(graphName : String) = {
		// Not implemented
	}

	def getName() : String = "graphx"

}