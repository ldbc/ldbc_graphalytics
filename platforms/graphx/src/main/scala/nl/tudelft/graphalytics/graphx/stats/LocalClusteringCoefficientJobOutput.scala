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