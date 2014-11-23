package org.tudelft.graphalytics.mapreducev2.conn;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tudelft.graphalytics.Graph;
import org.tudelft.graphalytics.algorithms.CDParameters;
import org.tudelft.graphalytics.mapreducev2.MapReduceJob;
import org.tudelft.graphalytics.mapreducev2.cd.DirectedCambridgeLPAJob;
import org.tudelft.graphalytics.mapreducev2.cd.UndirectedCambridgeLPAJob;
import org.tudelft.graphalytics.mapreducev2.common.GatherSnapSingleDirectedNodeInfoJob;

public class CONNJob extends MapReduceJob {
	private static final Logger log = LogManager.getLogger();
	
	// Stopping condition
	public enum Label {
        UPDATED
    }
	
    private boolean graphIsDirected;
    private boolean graphIsEdgeBased;
    private String inputPath;
    private String intermediatePath;
    private String outputPath;
	
	@Override
	public void parseGraphData(Graph graph, Object parameters) {
		graphIsDirected = graph.isDirected();
		graphIsEdgeBased = graph.isEdgeBased();
	}
    
    @Override
    public void setInputPath(String path) {
    	inputPath = path;
    }
    
    @Override
    public void setIntermediatePath(String path) {
    	intermediatePath = path;
    }
    
    @Override
    public void setOutputPath(String path) {
    	outputPath = path;
    }
    
    public int run(String[] args) throws Exception {
    	log.entry((Object[])args);
        
    	// Convert the input graph to the correct format for BFS
    	Tool conversionJob;
        if(graphIsDirected && graphIsEdgeBased) {
        	conversionJob = new GatherSnapSingleDirectedNodeInfoJob(inputPath, intermediatePath);
        } else {
        	return -1;
        }
        int result = ToolRunner.run(getConf(), conversionJob, args);
        if (result != 0)
        	return log.exit(result);
        
        // Run the BFS job
        if (graphIsDirected)
        	result = ToolRunner.run(getConf(),
        			new LabelDirectedConnectedComponentsJob(intermediatePath + "/prepared-graph",
        					intermediatePath,
        					outputPath),
        			args);
        else
        	result = ToolRunner.run(getConf(),
        			new LabelUndirectedConnectedComponentsJob(intermediatePath + "/prepared-graph",
        					intermediatePath,
        					outputPath),
        			args);
        
        return log.exit(result);
    }
}
