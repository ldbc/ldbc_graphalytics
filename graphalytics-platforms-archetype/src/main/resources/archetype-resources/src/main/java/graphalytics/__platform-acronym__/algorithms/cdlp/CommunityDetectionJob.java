#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.graphalytics.${platform-acronym}.algorithms.cdlp;

import java.util.List;
import org.apache.commons.configuration.Configuration;
import ${package}.graphalytics.domain.algorithms.CommunityDetectionLPParameters;
import ${package}.graphalytics.${platform-acronym}.${platform-name}Job;

/**
 *
 *
 * @author ${developer-name}
 */
public class CommunityDetectionJob extends ${platform-name}Job {
	private CommunityDetectionLPParameters params;
	
	public CommunityDetectionJob(Configuration config, String verticesPath, String edgesPath, boolean graphDirected,
								 CommunityDetectionLPParameters params, String jobId) {
		super(config, verticesPath, edgesPath, graphDirected, jobId);
		this.params = params;
	}

	@Override
	protected void addJobArguments(List<String> args) {
		args.add("cdlp");
		args.add("--max-iterations");
		args.add(Integer.toString(params.getMaxIterations()));
	}
}
