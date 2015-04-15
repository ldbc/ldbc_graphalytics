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
package nl.tudelft.graphalytics.giraph.bfs;

import org.apache.giraph.conf.LongConfOption;

/**
 * Configuration constants for breadth-first search on Giraph.
 * 
 * @author Tim Hegeman
 */
public final class BreadthFirstSearchConfiguration {
	
	/** Configuration key for the source vertex of the algorithm */
	public static final String SOURCE_VERTEX_KEY = "graphalytics.bfs.source-vertex";
	/** Configuration option for the source vertex of the algorithm */
	public static final LongConfOption SOURCE_VERTEX = new LongConfOption(
			SOURCE_VERTEX_KEY, -1, "Source vertex for the breadth first search algorithm");

	private BreadthFirstSearchConfiguration() {
	}
	
}
