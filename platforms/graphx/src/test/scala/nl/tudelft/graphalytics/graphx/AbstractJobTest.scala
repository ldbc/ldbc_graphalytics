package nl.tudelft.graphalytics.graphx

import java.net.URL

import org.apache.spark.SparkContext
import org.apache.spark.graphx.Graph
import org.scalatest.{Suite, BeforeAndAfter}

import scala.io.Source

/**
 * Trait for GraphX job integration tests. Handles Spark setup and cleanup.
 *
 * @author Tim Hegeman
 */
trait AbstractJobTest extends BeforeAndAfter { self : Suite =>

	after {
		// Based on org.apache.spark.LocalSparkContext
		// To avoid Akka rebinding to the same port, since it doesn't unbind immediately on shutdown
		System.clearProperty("spark.driver.port")
	}

	def performTest[VD, ED](job: GraphXJob[VD, ED], exampleFile: URL, asserts: (Graph[VD, ED]) => Unit) = {
		var sc: SparkContext = null
		try {
			// Create a Spark context for in-JVM computation
			sc = new SparkContext("local", "Graphalytics unit test")
			// Read the graph file
			val inputData = sc.parallelize(Source.fromURL(exampleFile).getLines().toSeq)
			// Execute the job
			val result = job.executeOnGraph(inputData)

			asserts(result)
		} finally {
			if (sc != null)
				sc.stop()
		}
	}

	def readGraphStructure(outputFile: URL): Map[Long, Set[Long]] = {
		Source.fromURL(outputFile).getLines().map {
			line => {
				val tokens = line.split(" ")
				(tokens(0).toLong, tokens.drop(1).map { _.toLong }.toSet)
			}
		}.toMap
	}

	def readGraphWithValues[V](outputFile: URL, parseFun: (Array[String] => V)) : Map[Long, V] = {
		Source.fromURL(outputFile).getLines().map {
			line => {
				val tokens = line.split(" ")
				(tokens(0).toLong, parseFun(tokens.drop(1)))
			}
		}.toMap
	}

}
