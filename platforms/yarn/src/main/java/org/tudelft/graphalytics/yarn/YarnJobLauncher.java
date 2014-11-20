package org.tudelft.graphalytics.yarn;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.tudelft.graphalytics.Graph;
import org.tudelft.graphalytics.yarn.common.GatherSnapSingleDirectedNodeInfoJob;

public abstract class YarnJobLauncher extends Configured implements Tool {
	
	private boolean graphIsDirected;
	private boolean graphIsEdgeBased;
	private String inputPath;
	private String intermediatePath;
	private String outputPath;
	protected Object parameters;
	protected int numMappers;
	protected int numReducers;
	
	public YarnJobLauncher() {
		graphIsDirected = graphIsEdgeBased = false;
		parameters = null;
		inputPath = intermediatePath = outputPath = "";
		numMappers = numReducers = -1;
	}

	public void parseGraphData(Graph graph, Object parameters) {
		graphIsDirected = graph.isDirected();
		graphIsEdgeBased = graph.isEdgeBased();
		
		this.parameters = parameters;
	}
    
    public void setInputPath(String path) {
    	inputPath = path;
    }
    
    public void setIntermediatePath(String path) {
    	intermediatePath = path;
    }
    
    public void setOutputPath(String path) {
    	outputPath = path;
    }
    
	public void setNumMappers(int numMappers) {
		this.numMappers = numMappers;
	}
	
	public void setNumReducers(int numReducers) {
		this.numReducers = numReducers;
	}
	
	@Override
    public int run(String[] args) throws Exception {
    	// Convert the input graph to the correct format for BFS
    	Tool conversionJob;
        if(graphIsDirected && graphIsEdgeBased) {
        	conversionJob = new GatherSnapSingleDirectedNodeInfoJob(inputPath, intermediatePath);
        } else {
        	return -1;
        }
        int result = ToolRunner.run(getConf(), conversionJob, args);
        if (result != 0)
        	return result;
        
        // Run the BFS job
        if (graphIsDirected)
        	result = ToolRunner.run(getConf(),
        			createDirectedJob(intermediatePath + "/prepared-graph",
        					intermediatePath,
        					outputPath),
        			args);
        else
        	result = ToolRunner.run(getConf(),
        			createUndirectedJob(intermediatePath + "/prepared-graph",
        					intermediatePath,
        					outputPath),
        			args);
        
        return result;
    }
	
	protected abstract Tool createDirectedJob(String input, String intermediate, String output);
	protected abstract Tool createUndirectedJob(String input, String intermediate, String output);
	
}
