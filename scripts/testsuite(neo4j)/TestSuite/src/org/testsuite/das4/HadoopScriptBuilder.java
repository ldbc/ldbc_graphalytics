package org.testsuite.das4;

import org.hadoop.test.jobs.utils.HadoopJob;

/*
   Generates Hadoop specific shell script parts.
*/
public class HadoopScriptBuilder {
    /*
        Clean
     */
    public String cleanLocal(String path, String fileName, boolean recursive) {
        StringBuilder cmd = new StringBuilder(path.length()+fileName.length()+16);
        cmd.append("cd ~/"+path+"\n");
        if(recursive)
            cmd.append("rm -rf *\n");
        else cmd.append("rm "+fileName);

        return cmd.toString();
    }

    public String cleanDfs(String path, String fileName, boolean recursive) {
        StringBuilder cmd = new StringBuilder(path.length()+fileName.length()+16);
        cmd.append("cd ~/scripts\n");
        cmd.append("./client.sh dfs -rm");
        if(recursive)
            cmd.append("r "+path);
        else
            cmd.append(" "+path+fileName);

        return cmd.toString();
    }

    public String cleanLogs() {
        StringBuilder cmd = new StringBuilder(100);
        cmd.append("cd ~/scripts/run/conf/logs/userlogs\n");
        cmd.append("rm -rf *\n");

        return cmd.toString();
    }

    /*
        Start
     */
    public String overwriteHadoopClusterConfig(String blockSize) {
        return "#Overwrite hadoop config\nsed -ie \'38s/.*/<value>"+blockSize+"</value>/\' hadoopConfig.xml"+"\n";
    }

    public String  overwriteHadoopConfig(String nodes) {
        return "cd ~/scripts\n#Overwrite cluster config\nsed -ie \'4s/.*/#$ -t 1-"+nodes+"/\' hadoop-cluster.sh"+"\n";
    }

    // sets priority to 0
    public String startHadoopClusterWithOverwrite(String blockSize, String nodes) {
        //String cmds = this.overwriteHadoopClusterConfig(blockSize); //overwrite block size in hadoop config doesn't work, dataset have to be copied with block size specification
        String cmds = this.overwriteHadoopConfig(nodes);
        return new String(cmds+"#Start Hadoop cluster\nqsub -p 0 hadoop-cluster.sh"+"\n");
    }

    public String checkBarriers() {
        StringBuilder cmd = new StringBuilder();
        cmd.append("#Checking for Cluster Barriers\n");
        cmd.append("until [ -f ./BARR* ]; do\n\techo \"Waiting for Barriers\"\n\tsleep 1\ndone\n"); //waiting for barriers LOOP
        cmd.append("echo \"Barriers found\"\n");
        cmd.append("until [ ! -f ./BARR* ]; do\n\t echo \"Barriers still present\"\n\tsleep 10\ndone\n");// waiting for barriers to disappear
        cmd.append("echo \"Cluster ready\"\n");
        cmd.append("#wait 10s just in case\nsleep 10\n");

        return cmd.toString();
    }

    /*
        Run
     */
    public String runStatsJob(HadoopJob job, String user, int jobCounter) {
        BashScriptBuilder bsb = new BashScriptBuilder();
        String datasetName = bsb.getDatasetName(job);
        StringBuilder cmd = new StringBuilder(500);
        cmd.append("cd ~/scripts\n");
        cmd.append("echo \"--------- "+datasetName+" ---------\"\n");
        cmd.append("echo 'copy "+datasetName+" to dfs'\n");
        cmd.append("./client.sh dfs -copyFromLocal /var/scratch/"+user+"/input/"+datasetName+" /local/hadoop.tmp."+user+"/\n");
        cmd.append("echo \"### Running stats for "+datasetName+" ###\"\n");
        cmd.append("./client.sh jar /home/mbiczak/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.GraphStatsJob "
                    +job.getType()+" "+job.getFormat()+" "
                    +job.getDelSrc()+" "+job.getDelIntermediate()+" "
                    +job.getMappers()+" "+job.getReducers()+" "
                    +"/local/hadoop.tmp."+user+"/"+datasetName+" /local/hadoop.tmp."+user+"/output_"+datasetName+"_"+jobCounter+"\n");
        cmd.append("echo \"### Fetching output from DFS ###\"\n");
        cmd.append("./client.sh dfs -copyToLocal "+"/local/hadoop.tmp."+user+"/output_"+datasetName+"_"+jobCounter
                   +" /var/scratch/"+user+"/output\n");
        cmd.append("echo \"### Cleaning wiki output in DFS ###\"\n");
        cmd.append("./client.sh dfs -rmr "+"/local/hadoop.tmp."+user+"/output_"+datasetName+"_"+jobCounter+"\n");
        cmd.append("echo \"Done\"\n" +
                   "echo \"--------- END "+datasetName+" ---------\"\n");

        return cmd.toString();
    }

    /*
        Shutdown
     */
    public String shutdownHadoop() {
        StringBuilder cmd = new StringBuilder(500);
        cmd.append("cd ~/scripts\n");
        cmd.append("./shutdown.sh");

        return cmd.toString();
    }
}
