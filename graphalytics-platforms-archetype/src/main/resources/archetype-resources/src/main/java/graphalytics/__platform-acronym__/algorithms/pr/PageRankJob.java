#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.graphalytics.${platform-acronym}.algorithms.pr;

import java.util.List;
import org.apache.commons.configuration.Configuration;
import ${package}.graphalytics.domain.algorithms.PageRankParameters;
import ${package}.graphalytics.${platform-acronym}.${platform-name}Job;

/**
 *
 *
 * @author ${developer-name}
 */
public class PageRankJob extends ${platform-name}Job {
	PageRankParameters params;

	public PageRankJob(Configuration config, String verticesPath, String edgesPath,
					   boolean graphDirected, PageRankParameters params, String jobId) {
		super(config, verticesPath, edgesPath, graphDirected, jobId);
		this.params = params;
	}

	@Override
	protected void addJobArguments(List<String> args) {
		args.add("pr");
		args.add("--damping-factor");
		args.add(Float.toString(params.getDampingFactor()));
		args.add("--max-iterations");
		args.add(Integer.toString(params.getNumberOfIterations()));
	}
}
