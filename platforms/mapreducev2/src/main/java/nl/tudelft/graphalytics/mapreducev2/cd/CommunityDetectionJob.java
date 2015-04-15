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
package nl.tudelft.graphalytics.mapreducev2.cd;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import nl.tudelft.graphalytics.domain.algorithms.CommunityDetectionParameters;
import nl.tudelft.graphalytics.mapreducev2.MapReduceJob;
import nl.tudelft.graphalytics.mapreducev2.cd.CommunityDetectionConfiguration.LABEL_STATUS;

import static nl.tudelft.graphalytics.mapreducev2.cd.CommunityDetectionConfiguration.HOP_ATTENUATION;
import static nl.tudelft.graphalytics.mapreducev2.cd.CommunityDetectionConfiguration.NODE_PREFERENCE;

/**
 * Job specification for community detection on MapReduce version 2.
 *
 * @author Tim Hegeman
 */
public class CommunityDetectionJob extends MapReduceJob<CommunityDetectionParameters> {

	private boolean directed;
	private boolean finished = false;
	
	public CommunityDetectionJob(String inputPath, String intermediatePath, String outputPath,
			CommunityDetectionParameters parameters, boolean directed) {
		super(inputPath, intermediatePath, outputPath, parameters);
		this.directed = directed;
	}

	@Override
	protected Class<?> getMapOutputKeyClass() {
		return Text.class;
	}

	@Override
	protected Class<?> getMapOutputValueClass() {
		return Text.class;
	}

	@Override
	protected Class<?> getOutputKeyClass() {
		return NullWritable.class;
	}

	@Override
	protected Class<?> getOutputValueClass() {
		return Text.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends InputFormat> getInputFormatClass() {
		return TextInputFormat.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends OutputFormat> getOutputFormatClass() {
		return TextOutputFormat.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Mapper> getMapperClass() {
		return (directed ?
				DirectedCambridgeLPAMap.class :
				UndirectedCambridgeLPAMap.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Reducer> getReducerClass() {
		return (directed ?
				DirectedCambridgeLPAReducer.class :
				UndirectedCambridgeLPAReducer.class);
	}

	@Override
	protected boolean isFinished() {
		return finished || getIteration() >= getParameters().getMaxIterations();
	}
	
	@Override
	protected void setConfigurationParameters(JobConf jobConfiguration) {
		super.setConfigurationParameters(jobConfiguration);
		jobConfiguration.set(HOP_ATTENUATION, Float.toString(getParameters().getHopAttenuation()));
    	jobConfiguration.set(NODE_PREFERENCE, Float.toString(getParameters().getNodePreference()));
	}

	@Override
	protected void processJobOutput(RunningJob jobExecution) throws IOException {
		Counters jobCounters = jobExecution.getCounters();
    	long nodesVisisted = jobCounters.getCounter(LABEL_STATUS.CHANGED);
    	if (nodesVisisted == 0)
    		finished = true;
    	
    	System.out.println("\n************************************");
        System.out.println("* CD Iteration "+ getIteration() +" FINISHED *");
        System.out.println("* Nodes visited: " + nodesVisisted + " *");
        System.out.println("************************************\n");
	}
	
	
	
}
