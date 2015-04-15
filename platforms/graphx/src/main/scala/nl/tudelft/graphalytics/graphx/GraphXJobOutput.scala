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
