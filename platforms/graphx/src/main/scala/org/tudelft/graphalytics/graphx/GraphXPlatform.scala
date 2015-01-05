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
		
		pathsOfGraphs += (graph.getName -> hdfsPath.toUri.getPath)
	}

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

	def deleteGraph(graphName : String) = {
		// Not implemented
	}

	def getName() : String = "graphx"

}
