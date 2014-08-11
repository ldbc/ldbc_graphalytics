package org.tudelft.graphalytics.yarn.bfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tudelft.graphalytics.yarn.YarnJob;
import org.tudelft.graphalytics.yarn.common.GatherDelftSingleDirectedNodeInfoJob;
import org.tudelft.graphalytics.yarn.common.GatherDelftSingleUndirectedNodeInfoJob;
import org.tudelft.graphalytics.yarn.common.GatherFilteredSingleNodeInfoJob;
import org.tudelft.graphalytics.yarn.common.GatherSnapSingleDirectedNodeInfoJob;
import org.tudelft.graphalytics.yarn.common.GatherSnapSingleUndirectedNodeInfoJob;

import java.io.IOException;

public class BFSJob extends YarnJob {
	private static final Logger log = LogManager.getLogger();
	
    // src ID
    public static final String SRC_ID_KEY = "SRC_ID";
    public static String SRC_ID;

    public int run(String[] args) throws Exception {
    	log.entry((Object[])args);
        if(args.length != 9) {
            System.out.println("These parameters are REQUIRED for the JOB");
            System.out.println("1 - graph type = directed || undirected");
            System.out.println("2 - format type = snap || delft || filtered");
            System.out.println("3 - del src input = boolean");
            System.out.println("4 - del intermediate results = boolean");
            System.out.println("5 - #mappers = int");
            System.out.println("6 - #reducers = int");
            System.out.println("7 - input dataset = path");
            System.out.println("8 - output = path");
            System.out.println("9 - src node ID = int");
            System.out.println("JOB WILL BE TERMINATED");

            return 0;
        }

        String graphType = args[0];
        BFSJob.SRC_ID = args[8];
        String[] parameters = new String[args.length - 1];
        for(int i=0; i<parameters.length; i++)
            parameters[i] = args[i+1];

        if(graphType.equals("undirected"))
            return this.undirectedBFS(parameters);
        else
            return this.directedBFS(parameters);
    }

    public int undirectedBFS(String[] args) throws Exception {
        String formatType = args[0];
        String[] parameters = new String[args.length - 1];
        for(int i=0; i<parameters.length; i++)
            parameters[i] = args[i+1];

        if(formatType.equals("delft"))
            ToolRunner.run(new GatherDelftSingleUndirectedNodeInfoJob(), parameters);
        else if(formatType.equals("snap"))
        	ToolRunner.run(new Configuration(), new GatherSnapSingleUndirectedNodeInfoJob(), parameters);
        else if(formatType.equals("filtered")) {
        	ToolRunner.run(new GatherFilteredSingleNodeInfoJob(), parameters);
        }
        else
            throw new IOException("Unsupported graph format = "+formatType);

        // init iteration
        ToolRunner.run(new UndirectedBFSJob(), parameters);

        return 0;
    }

    public int directedBFS(String[] args) throws Exception {
        String formatType = args[0];
        String[] parameters = new String[args.length - 1];
        for(int i=0; i<parameters.length; i++)
            parameters[i] = args[i+1];

        if(formatType.equals("delft"))
        	ToolRunner.run(new GatherDelftSingleDirectedNodeInfoJob(), parameters);
        else if(formatType.equals("snap"))
        	ToolRunner.run(new Configuration(true), new GatherSnapSingleDirectedNodeInfoJob(), parameters);
        else if(formatType.equals("filtered")) {
        	ToolRunner.run(new GatherFilteredSingleNodeInfoJob(), parameters);
        }
        else
            throw new IOException("Unsupported graph format = "+formatType);

        // init iteration
        ToolRunner.run(new DirectedBFSJob(), parameters);

        return 0;
    }

    public static void main(String[] args) throws Exception{
        int ret = ToolRunner.run(new BFSJob(), args);

        System.exit(ret);
    }
}
