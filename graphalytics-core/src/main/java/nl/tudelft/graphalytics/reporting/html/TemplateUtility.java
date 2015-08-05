/*
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
package nl.tudelft.graphalytics.reporting.html;

import nl.tudelft.graphalytics.domain.Graph;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Utility class for use in the HTML report templates.
 *
 * @author Tim Hegeman
 */
public class TemplateUtility {

	/**
	 * Generates a string containing the name and size of a graph, with format: "graphname (X vertices, Y edges)".
	 *
	 * @param graph the graph to generate a human-readable string for
	 * @return a string representation of the graph name and size
	 */
	public String formatGraphNameSize(Graph graph) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setGroupingSeparator('\u202F');
		DecimalFormat df = new DecimalFormat("#,###", symbols);
		StringBuffer sb = new StringBuffer();
		sb.append(graph.getName()).append(" (")
				.append(df.format(graph.getNumberOfVertices())).append(" vertices, ")
				.append(df.format(graph.getNumberOfEdges())).append(" edges)");
		return sb.toString();
	}

}
