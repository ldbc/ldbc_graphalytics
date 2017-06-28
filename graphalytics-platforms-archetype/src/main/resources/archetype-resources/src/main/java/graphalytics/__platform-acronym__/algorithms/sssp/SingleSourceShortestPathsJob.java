#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.graphalytics.${platform-acronym}.algorithms.sssp;

import java.util.List;
import org.apache.commons.configuration.Configuration;
import ${package}.graphalytics.domain.algorithms.SingleSourceShortestPathsParameters;
import ${package}.graphalytics.${platform-acronym}.${platform-name}Job;

/**
 *
 *
 * @author ${developer-name}
 */
public class SingleSourceShortestPathsJob extends ${platform-name}Job {

	SingleSourceShortestPathsParameters params;

	public SingleSourceShortestPathsJob(Configuration config, String verticesPath, String edgesPath, boolean graphDirected,
										SingleSourceShortestPathsParameters params, String jobId) {
		super(config, verticesPath, edgesPath, graphDirected, jobId);
		this.params = params;
	}

	@Override
	protected void addJobArguments(List<String> args) {
		args.add("sssp");
		args.add("--source-vertex");
		args.add(Long.toString(params.getSourceVertex()));
	}
}
