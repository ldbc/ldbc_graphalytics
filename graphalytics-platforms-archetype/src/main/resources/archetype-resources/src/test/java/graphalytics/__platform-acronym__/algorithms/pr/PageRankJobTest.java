#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * Copyright 2015 - 2017 Atlarge Research Team,
 * operating at Technische Universiteit Delft
 * and Vrije Universiteit Amsterdam, the Netherlands.
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
package ${package}.graphalytics.${platform-acronym}.algorithms.pr;

import java.io.File;

import ${package}.graphalytics.domain.algorithms.PageRankParameters;
import ${package}.graphalytics.${platform-acronym}.Utils;
import ${package}.graphalytics.validation.GraphStructure;
import ${package}.graphalytics.validation.algorithms.pr.PageRankOutput;
import ${package}.graphalytics.validation.algorithms.pr.PageRankValidationTest;
import org.junit.Ignore;

/**
 * Validation tests for the PageRank implementation in ${platform-name}.
 *
 * @author ${developer-name}
 */
@Ignore
public class PageRankJobTest extends PageRankValidationTest {

	@Override
	public PageRankOutput executeDirectedPageRank(GraphStructure graph, PageRankParameters parameters)
			throws Exception {
		return execute(graph, parameters, true);
	}

	@Override
	public PageRankOutput executeUndirectedPageRank(GraphStructure graph, PageRankParameters parameters)
			throws Exception {
		return execute(graph, parameters, false);
	}
	
	private PageRankOutput execute(GraphStructure graph, PageRankParameters parameters, boolean directed)
			throws Exception {
		File edgesFile = File.createTempFile("edges.", ".txt");
		File verticesFile = File.createTempFile("vertices.", ".txt");
		File outputFile = File.createTempFile("output.", ".txt");

		Utils.writeEdgeToFile(graph, directed, edgesFile);
		Utils.writeVerticesToFile(graph, verticesFile);

		String jobId = "RandomJobId";
		
		PageRankJob job = new PageRankJob(
				Utils.loadConfiguration(), verticesFile.getAbsolutePath(), edgesFile.getAbsolutePath(), directed, parameters, jobId);
		job.setOutputFile(outputFile);
		job.run();
		
		return new PageRankOutput(Utils.readResults(outputFile, Double.class));
	}

}
