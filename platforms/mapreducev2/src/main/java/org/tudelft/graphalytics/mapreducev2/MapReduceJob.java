package org.tudelft.graphalytics.mapreducev2;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.tudelft.graphalytics.Graph;

public abstract class MapReduceJob extends Configured implements Tool {

	public abstract void parseGraphData(Graph graph, Object parameters);
	public abstract void setInputPath(String path);
	public abstract void setIntermediatePath(String path);
	public abstract void setOutputPath(String path);
	
}
