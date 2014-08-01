package org.hadoop.test.jobs;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.hadoop.test.jobs.tasks.bfs.init.DirectedBFSJob;
import org.hadoop.test.jobs.tasks.bfs.init.UndirectedBFSJob;
import org.hadoop.test.jobs.tasks.utils.GatherFilteredSingleNodeInfoJob;
import org.hadoop.test.jobs.tasks.utils.directed.GatherDelftSingleDirectedNodeInfoJob;
import org.hadoop.test.jobs.tasks.utils.directed.GatherSnapSingleDirectedNodeInfoJob;
import org.hadoop.test.jobs.tasks.utils.undirected.GatherDelftSingleUndirectedNodeInfoJob;
import org.hadoop.test.jobs.tasks.utils.undirected.GatherSnapSingleUndirectedNodeInfoJob;

import java.io.IOException;

public class BFSJob extends Configured implements Tool {
    // src ID
    public static final String SRC_ID_KEY = "SRC_ID";
    public static String SRC_ID;

    public int run(String[] args) throws Exception {
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
            new GatherDelftSingleUndirectedNodeInfoJob().run(parameters);
        else if(formatType.equals("snap"))
            new GatherSnapSingleUndirectedNodeInfoJob().run(parameters);
        else if(formatType.equals("filtered")) {
            new GatherFilteredSingleNodeInfoJob().run(parameters);
        }
        else
            throw new IOException("Unsupported graph format = "+formatType);

        // init iteration
        new UndirectedBFSJob().run(parameters);

        return 0;
    }

    public int directedBFS(String[] args) throws Exception {
        String formatType = args[0];
        String[] parameters = new String[args.length - 1];
        for(int i=0; i<parameters.length; i++)
            parameters[i] = args[i+1];

        if(formatType.equals("delft"))
            new GatherDelftSingleDirectedNodeInfoJob().run(parameters);
        else if(formatType.equals("snap"))
            new GatherSnapSingleDirectedNodeInfoJob().run(parameters);
        else if(formatType.equals("filtered")) {
            new GatherFilteredSingleNodeInfoJob().run(parameters);
        }
        else
            throw new IOException("Unsupported graph format = "+formatType);

        // init iteration
        new DirectedBFSJob().run(parameters);

        return 0;
    }

    public static void main(String[] args) throws Exception{
        int ret = ToolRunner.run(new BFSJob(), args);

        System.exit(ret);
    }
}
