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
package nl.tudelft.graphalytics.mapreducev2;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import nl.tudelft.graphalytics.domain.Graph;

/**
 * Base class for launching MapReduce jobs, with hooks to create algorithm-specific
 * jobs. Every algorithm in the Graphalytics MapReduce suite must have a class that
 * inherits from this.
 *
 * @author Tim Hegeman
 */
public abstract class MapReduceJobLauncher extends Configured implements Tool {
	
	private boolean graphIsDirected;
	private String inputPath;
	private String intermediatePath;
	private String outputPath;
	protected Object parameters;
	protected int numMappers;
	protected int numReducers;

	/**
	 * Default constructor.
	 */
	public MapReduceJobLauncher() {
		graphIsDirected = false;
		parameters = null;
		inputPath = intermediatePath = outputPath = "";
		numMappers = numReducers = -1;
	}

	/**
	 * @param graph the input graph specification
	 * @param parameters the graph-specific parameters for this algorithm
	 */
	public void parseGraphData(Graph graph, Object parameters) {
		graphIsDirected = graph.getGraphFormat().isDirected();

		this.parameters = parameters;
	}

	/**
	 * @param path the path on HDFS to the input graph
	 */
    public void setInputPath(String path) {
    	inputPath = path;
    }

	/**
	 * @param path the path on HDFS to store intermediate data in
	 */
    public void setIntermediatePath(String path) {
    	intermediatePath = path;
    }

	/**
	 * @param path the path on HDFS to store the output in
	 */
    public void setOutputPath(String path) {
    	outputPath = path;
    }

	/**
	 * @param numMappers the number of mappers to request for each job
	 */
	public void setNumMappers(int numMappers) {
		this.numMappers = numMappers;
	}

	/**
	 * @param numReducers the number of reducers to request for each job
	 */
	public void setNumReducers(int numReducers) {
		this.numReducers = numReducers;
	}

	/**
	 * Create and launch the MapReduce job(s) for the implemented algorithm.
	 *
	 * @param args unused
	 * @return job exit code
	 * @throws Exception if the job failed unexpectedly
	 */
	@Override
    public int run(String[] args) throws Exception {
        // Create the appropriate job
		MapReduceJob<?> job;
        if (graphIsDirected)
        	job = createDirectedJob(inputPath, intermediatePath, outputPath);
        else
        	job = createUndirectedJob(inputPath, intermediatePath, outputPath);
        
        // Update configuration
        job.setNumMappers(numMappers);
        job.setNumReducers(numReducers);
        
        // Run it!
    	return ToolRunner.run(getConf(), job, args);
    }

	/**
	 * @param input the input path as set using {@link #setInputPath(String) setInputPath}
	 * @param intermediate the intermediate path as set using {@link #setIntermediatePath(String)} setIntermediatePath}
	 * @param output the output path as set using {@link #setOutputPath(String) setOutputPath}
	 * @return a MapReduceJob of the implemented algorithm for a directed graph
	 */
	protected abstract MapReduceJob<?> createDirectedJob(String input, String intermediate, String output);

	/**
	 * @param input the input path as set using {@link #setInputPath(String) setInputPath}
	 * @param intermediate the intermediate path as set using {@link #setIntermediatePath(String)} setIntermediatePath}
	 * @param output the output path as set using {@link #setOutputPath(String) setOutputPath}
	 * @return a MapReduceJob of the implemented algorithm for an undirected graph
	 */
	protected abstract MapReduceJob<?> createUndirectedJob(String input, String intermediate, String output);
	
}
