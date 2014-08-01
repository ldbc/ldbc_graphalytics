package org.testsuite.job.utils;

import org.hadoop.test.jobs.utils.HadoopJob;
import org.hadoop.test.jobs.utils.HadoopJobExecutionStats;
import org.test.neo4j.utils.Neo4Job;
import org.test.neo4j.utils.Neo4JobExecutionStats;
import org.test.postgresql.job.PsqlJob;
import org.test.postgresql.job.PsqlJobExecutionStats;
import org.testsuite.job.Job;

public class JobsAdapter {
    /*
        Neo4J adapters
     */
    public Neo4Job job2NeoAdapt(Job job) {
        Neo4Job neoJob = new Neo4Job();

        neoJob.setEdgeCount(job.getEdgeCount());
        neoJob.setJobClass(job.getJobClass());
        neoJob.setJobExecutionStats(this.jobStats2NeoStats(job.getJobExecutionStats()));
        neoJob.setJobInput(job.getJobInput());
        neoJob.setJobOutput(job.getJobOutput());
        neoJob.setJobParams(job.getJobParams());
        neoJob.setJobRuns(job.getJobRuns());
        neoJob.setPrintBenchmark(job.getPrintBenchmark());
        neoJob.setPrintIntermediateResults(job.isPrintIntermediateResults());
        neoJob.setInit(job.isInit());
        neoJob.setType(job.getType());
        neoJob.setFormat(job.getFormat());
        neoJob.setSrcID(job.getSrcId());
        neoJob.setSamplingBudget(job.getSampling());
        neoJob.setNrWalkers(job.getNrWalkers());
        neoJob.setRndJumpThershold(job.getRndJumpThershold());
        neoJob.setpRation(job.getpRation());
        neoJob.setrRation(job.getrRation());
        neoJob.setNewVertices(job.getNewVertices());
        neoJob.setMaxID(job.getMaxID());
        neoJob.setHoops(job.getHoops());
        neoJob.setMParam(job.getMParam());
        neoJob.setDelta(job.getDelta());
        neoJob.setIterations(job.getIterations());

        return neoJob;
    }

    public void mergeNeoJobWithJob(Neo4Job neoJob, Job job) {
        job.setJobExecutionStats(this.neoStats2JobStats(neoJob.getJobExecutionStats()));
    }

    private Neo4JobExecutionStats jobStats2NeoStats(JobExecutionStats job) {
        Neo4JobExecutionStats neoStats = new Neo4JobExecutionStats();
        neoStats.setEdgeSize(job.getEdgeSize());
        neoStats.setNodeSize(job.getNodeSize());
        neoStats.setTime(job.getTime());

        return neoStats;
    }

    private JobExecutionStats neoStats2JobStats(Neo4JobExecutionStats neoJob) {
        JobExecutionStats jobStats = new JobExecutionStats();
        jobStats.setEdgeSize(neoJob.getEdgeSize());
        jobStats.setNodeSize(neoJob.getNodeSize());
        jobStats.setTime(neoJob.getTime());

        return jobStats;
    }

    /*
        PSQL adapter
     */
    public PsqlJob job2PsqlAdapt(Job job) {
        PsqlJob psqlJob = new PsqlJob();

        psqlJob.setInit(job.isInit());
        psqlJob.setDbLogin(job.getDbLogin());
        psqlJob.setDbName(job.getDbName());
        psqlJob.setDbPassword(job.getDbPassword());
        psqlJob.setEdgeCount(job.getEdgeCount());
        psqlJob.setJobClass(job.getJobClass());
        psqlJob.setJobExecutionStats(this.jobStats2PsqlStats(job.getJobExecutionStats()));
        psqlJob.setJobInput(job.getJobInput());
        psqlJob.setJobRuns(job.getJobRuns());
        psqlJob.setPlotTitle(job.getPlotTitle());
        psqlJob.setPrintBenchmark(job.getPrintBenchmark());
        psqlJob.setX(job.getX());
        psqlJob.setDbUrl(job.getDbURL());
        psqlJob.setType(job.getType());
        psqlJob.setFormat(job.getFormat());

        return psqlJob;
    }

    public void mergePsqlJobWithJob(PsqlJob psqlJob, Job job) {
        job.setJobExecutionStats(this.psqlStats2JobStats(psqlJob.getJobExecutionStats()));
    }

    private PsqlJobExecutionStats jobStats2PsqlStats(JobExecutionStats job) {
        PsqlJobExecutionStats psqlStats = new PsqlJobExecutionStats();
        psqlStats.setEdgeSize(job.getEdgeSize());
        psqlStats.setNodeSize(job.getNodeSize());
        psqlStats.setTime(job.getTime());

        return psqlStats;
    }

    private JobExecutionStats psqlStats2JobStats(PsqlJobExecutionStats psqlJob) {
        JobExecutionStats jobStats = new JobExecutionStats();
        jobStats.setEdgeSize(psqlJob.getEdgeSize());
        jobStats.setNodeSize(psqlJob.getNodeSize());
        jobStats.setTime(psqlJob.getTime());

        return jobStats;
    }

    /*
        Hadoop adapter
     */
    public HadoopJob job2HadoopAdapt(Job job) {
        HadoopJob hadoopJob = new HadoopJob();

        hadoopJob.setInit(job.isInit());
        hadoopJob.setEdgeCount(job.getEdgeCount());
        hadoopJob.setJobClass(job.getJobClass());
        hadoopJob.setJobExecutionStats(this.jobStats2HadoopStats(job.getJobExecutionStats()));
        hadoopJob.setJobInput(job.getJobInput());
        hadoopJob.setJobOutput(job.getJobOutput());
        hadoopJob.setJobRuns(job.getJobRuns());
        hadoopJob.setPlotTitle(job.getPlotTitle());
        hadoopJob.setPrintBenchmark(job.getPrintBenchmark());
        hadoopJob.setX(job.getX());
        hadoopJob.setType(job.getType());
        hadoopJob.setFormat(job.getFormat());
        hadoopJob.setDelSrc(String.valueOf(job.isDelSrc()));
        hadoopJob.setDelIntermediate(String.valueOf(job.isDelIntermediate()));
        hadoopJob.setMappers(job.getMappers());
        hadoopJob.setReducers(job.getReducers());

        return hadoopJob;
    }

    public void mergeHadoopJobWithJob(HadoopJob hadoopJob, Job job) {
        job.setJobExecutionStats(this.psqlStats2JobStats(hadoopJob.getJobExecutionStats()));
    }

    private HadoopJobExecutionStats jobStats2HadoopStats(JobExecutionStats job) {
        HadoopJobExecutionStats hadoopJobExecutionStats = new HadoopJobExecutionStats();
        hadoopJobExecutionStats.setEdgeSize(job.getEdgeSize());
        hadoopJobExecutionStats.setNodeSize(job.getNodeSize());
        hadoopJobExecutionStats.setTime(job.getTime());

        return hadoopJobExecutionStats;
    }

    private JobExecutionStats psqlStats2JobStats(HadoopJobExecutionStats hadoopJobExecutionStats) {
        JobExecutionStats jobStats = new JobExecutionStats();
        jobStats.setEdgeSize(hadoopJobExecutionStats.getEdgeSize());
        jobStats.setNodeSize(hadoopJobExecutionStats.getNodeSize());
        jobStats.setTime(hadoopJobExecutionStats.getTime());

        return jobStats;
    }
}
