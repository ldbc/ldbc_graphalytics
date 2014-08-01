package org.testsuite.tool;

import org.apache.log4j.Logger;
import org.testsuite.job.Job;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class HadoopGraphJobs {
    static Logger log = Logger.getLogger(HadoopGraphJobs.class.getName());

    private class ArgsCreator {

        public String[] mapJobParams2Args(Job job) {
            String[] args = new String[2];
            args[0] = job.getJobInput();
            args[1] = job.getJobOutput();

            return args;
        }
    }

    // todo path to hadoop and jar MUST be dynamic (define in config?)
    // todo benchmark parse Hadoop logs after execution
    public Job runner(Job job) {
        try{
            if(job.getJobClass().equals("Stats")) {
                long t0 = System.currentTimeMillis();
                ProcessBuilder processBuilder =
                        new ProcessBuilder("/home/alien01/Projects/cloud/hadoop-0.20.203.0/bin/hadoop",
                                           "jar", "/home/alien01/delft/MGR/competition/Hadoop/hadoopJobs.jar",
                                           "org.hadoop.test.jobs.GraphStatsJob",
                                           job.getType(), job.getFormat(),
                                           String.valueOf(job.isDelSrc()), String.valueOf(job.isDelIntermediate()),
                                           String.valueOf(job.getMappers()), String.valueOf(job.getReducers()),
                                           job.getJobInput(), job.getJobOutput());
                processBuilder.redirectErrorStream(true);
                Process hadoop = processBuilder.start();

                //redirect childs stream to parent
                BufferedReader hadoopOutput = new BufferedReader(new InputStreamReader(hadoop.getInputStream()));
                String line;
                while ((line = hadoopOutput.readLine()) != null)
                  log.info("HADOOP: "+line);

                hadoop.waitFor();

                // job time benchmark
                long t1 = System.currentTimeMillis();
                double elapsedTimeSeconds = (t1 - t0)/1000.0;
                job.getJobExecutionStats().setTime(elapsedTimeSeconds);
                //todo this is CHEATING -> parse logs n read value
                job.getJobExecutionStats().setEdgeSize(job.getEdgeCount());
            }
            else if(job.getJobClass().equals("Detection"))
                log.info("Detection");
            else if(job.getJobClass().equals("Evolution"))
                log.info("Evolution");
        }catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return job;
    }
}
