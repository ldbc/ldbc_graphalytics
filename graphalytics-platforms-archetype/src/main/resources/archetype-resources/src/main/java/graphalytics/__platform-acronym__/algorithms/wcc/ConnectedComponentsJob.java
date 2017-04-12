#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.graphalytics.${platform-acronym}.algorithms.wcc;

import java.util.List;
import org.apache.commons.configuration.Configuration;
import ${package}.graphalytics.${platform-acronym}.${platform-name}Job;

/**
 *
 *
 * @author ${developer-name}
 */
public class ConnectedComponentsJob extends ${platform-name}Job {

	public ConnectedComponentsJob(Configuration config, String verticesPath, String edgesPath, boolean graphDirected, String jobId) {
		super(config, verticesPath, edgesPath, graphDirected, jobId);
	}

	@Override
	protected void addJobArguments(List<String> args) {
		args.add("wcc");
	}
}
