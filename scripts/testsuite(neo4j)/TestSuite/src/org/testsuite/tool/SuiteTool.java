package org.testsuite.tool;

import org.apache.log4j.Logger;
import org.hadoop.test.jobs.utils.HadoopJob;
import org.test.neo4j.GraphJobs;

import org.test.neo4j.utils.Neo4Job;
import org.test.postgresql.PsqlGraphJobs;
import org.test.postgresql.job.PsqlJob;
import org.testsuite.das4.BashScriptBuilder;
import org.testsuite.exceptions.TestSuiteException;
import org.testsuite.job.Job;
import org.testsuite.job.Suite;
import org.testsuite.job.utils.BenchmarkPrinter;
import org.testsuite.job.utils.JobsAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SuiteTool {
    static Logger log = Logger.getLogger(SuiteTool.class.getName());

    public List<Suite> runTestSuites(List<Suite> suites) throws IOException, TestSuiteException {
        Iterator<Suite> suiteIterator = suites.iterator();
        while (suiteIterator.hasNext())
            this.runSuite(suiteIterator.next());

        log.info("## All "+suites.get(0).getTechnology()+" suites are completed ##");

        return suites;
    }

    private Suite runSuite(Suite suite) throws IOException, TestSuiteException {
        // create DAS4 scripts for WHOLE suites
        if((suite.getTechnology().equals(Suite.Technology.HADOOP_V1) ||
           suite.getTechnology().equals(Suite.Technology.HADOOP_V2) ||
           suite.getTechnology().equals(Suite.Technology.GIRAPH)) && "das".equals(suite.getExe())) {
            this.invokeSuiteDas4Script(suite);
        } else {
            // invoke each job in a suite LOCALLY (including hadoop or giraph)
            Iterator<Job> jobIterator = suite.getSuiteJobs().iterator();
            log.info("SUITE: "+suite.getStartMasg());

            while (jobIterator.hasNext())
                this.runJob(suite.getTechnology(), jobIterator.next());

            log.info("SUITE_END: "+suite.getEndMsg());
            log.info("-------------------- Suite COMPLETED --------------------");
        }

        return suite;
    }

    private Job runJob(Suite.Technology technology, Job job) throws IOException, TestSuiteException{
        JobsAdapter adapter = new JobsAdapter();

        switch (technology) {
            case NEO4J:
                Neo4Job neoJob = adapter.job2NeoAdapt(job);
                new GraphJobs().runner(neoJob);
                adapter.mergeNeoJobWithJob(neoJob, job);
                break;

            case HADOOP_V1: //used for code debugging in local mode
                new HadoopGraphJobs().runner(job);
                break;

            case SQL:
                PsqlJob psqlJob = adapter.job2PsqlAdapt(job);
                new PsqlGraphJobs().runner(psqlJob);
                adapter.mergePsqlJobWithJob(psqlJob, job);
                break;

            default:
                throw new TestSuiteException("Suite.Technology unknown, can not execute Job = "+job.getJobClass());
        }

        // print benchmark here cause if runsN -> print aggregated results
        if(job.getPrintBenchmark())
            BenchmarkPrinter.printBenchmarkData(job.getJobExecutionStats());

        log.info("-------------------- Job Runs COMPLETED --------------------");

        return job;
    }

    // todo switch() { case: Hadoop_V2 && Giraph}
    private Suite invokeSuiteDas4Script(Suite suite) {
        JobsAdapter adapter = new JobsAdapter();

        List<Job> jobs = suite.getSuiteJobs();

        switch (suite.getTechnology()) {
            case HADOOP_V1:
                List<HadoopJob> hadoopJobs = new ArrayList<HadoopJob>();
                // adapt
                for(Job job : jobs)
                    hadoopJobs.add(adapter.job2HadoopAdapt(job));
                new BashScriptBuilder().runHadoopJob(hadoopJobs, suite);
                // merge
                System.out.println("Hadoop merge hadoopJob with Job MUST be asynchronous");
                //for(int i=0; i< jobs.size(); i++)
                //    adapter.mergeHadoopJobWithJob(hadoopJobs.get(i), jobs.get(i));
                break;

            case HADOOP_V2:
                System.out.println("ToDo Hadoop_V2");
                break;

            case GIRAPH:
                System.out.println("ToDo GIRAPH");
                break;
        }

        return suite;
    }
}
