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
package nl.tudelft.graphalytics.mapreducev2.stats;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import nl.tudelft.graphalytics.mapreducev2.MapReduceJob;
import nl.tudelft.graphalytics.mapreducev2.common.UndirectedNode;
import nl.tudelft.graphalytics.mapreducev2.common.UndirectedNodeNeighbourhood;
import nl.tudelft.graphalytics.mapreducev2.common.UndirectedNodeNeighbourTextInputFormat;

/**
 * @author Tim Hegeman
 */
public class UndirectedStatsJob extends MapReduceJob<Object> {

	public UndirectedStatsJob(String inputPath, String intermediatePath, String outputPath, Object parameters) {
		super(inputPath, intermediatePath, outputPath, parameters);
	}

	@Override
	protected Class<?> getMapOutputKeyClass() {
		return Text.class;
	}

	@Override
	protected Class<?> getMapOutputValueClass() {
		return (getIteration() == 1 ?
				UndirectedNode.class :
				DoubleAverage.class);
	}

	@Override
	protected Class<?> getOutputKeyClass() {
		return NullWritable.class;
	}

	@Override
	protected Class<?> getOutputValueClass() {
		return (getIteration() == 1 ?
				UndirectedNodeNeighbourhood.class :
				Text.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends InputFormat> getInputFormatClass() {
		return (getIteration() == 1 ?
				TextInputFormat.class :
				UndirectedNodeNeighbourTextInputFormat.class);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends OutputFormat> getOutputFormatClass() {
		return TextOutputFormat.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Mapper> getMapperClass() {
		return (getIteration() == 1 ?
				GatherUndirectedNodeNeighboursInfoMap.class :
				UndirectedStatsCCMap.class);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Reducer> getCombinerClass() {
		return (getIteration() == 1 ?
				null :
				DoubleAverageCombiner.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Reducer> getReducerClass() {
		return (getIteration() == 1 ?
				GatherUndirectedNodeNeighboursInfoReducer.class :
				StatsCCReducer.class);
	}

	@Override
	protected boolean isFinished() {
		return (getIteration() >= 2);
	}

	@Override
	protected void setConfigurationParameters(JobConf jobConfiguration) {
		
	}

	@Override
	protected void processJobOutput(RunningJob jobExecution) throws IOException {
		if (getIteration() == 1) {
			System.out.println("\n*****************************************");
	        System.out.println("* node neighbourhood retrieved FINISHED *");
	        System.out.println("*****************************************\n");
		} else {
			System.out.println("\n***********************************************************");
	        System.out.println("* basic stats and average clustering coefficient FINISHED *");
	        System.out.println("***********************************************************\n");
		}
	}

}
