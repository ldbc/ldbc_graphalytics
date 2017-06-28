#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.graphalytics.${platform-acronym}.algorithms.lcc;

import java.util.List;
import org.apache.commons.configuration.Configuration;
import ${package}.graphalytics.${platform-acronym}.${platform-name}Job;

/**
 *
 *
 * @author ${developer-name}
 */
public class LocalClusteringCoefficientJob extends ${platform-name}Job {

	public LocalClusteringCoefficientJob(Configuration config, String verticesPath, String edgesPath, boolean graphDirected, String jobId) {
		super(config, verticesPath, edgesPath, graphDirected, jobId);
	}

	@Override
	protected void addJobArguments(List<String> args) {
		args.add("lcc");
	}
}
