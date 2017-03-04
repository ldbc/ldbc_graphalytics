#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.graphalytics.${platform-acronym}.algorithms.bfs;

import java.util.List;
import org.apache.commons.configuration.Configuration;
import ${package}.graphalytics.domain.algorithms.BreadthFirstSearchParameters;
import ${package}.graphalytics.${platform-acronym}.${platform-name}Job;

/**
 *
 *
 * @author ${developer-name}
 */
public class BreadthFirstSearchJob extends ${platform-name}Job {
	
	BreadthFirstSearchParameters params;

	public BreadthFirstSearchJob(Configuration config, String verticesPath, String edgesPath, boolean graphDirected,
								 BreadthFirstSearchParameters params, String jobId) {
		super(config, verticesPath, edgesPath, graphDirected, jobId);
		this.params = params;
	}

	@Override
	protected void addJobArguments(List<String> args) {
		args.add("bfs");
		args.add("--source-vertex");
		args.add(Long.toString(params.getSourceVertex()));
	}
}
