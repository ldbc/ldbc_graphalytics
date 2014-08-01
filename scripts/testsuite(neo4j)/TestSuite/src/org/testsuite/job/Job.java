package org.testsuite.job;

import org.testsuite.job.utils.JobExecutionStats;

import java.util.ArrayList;
import java.util.List;

// this class is a mix of all jobs config sry :(, thx java collections
public class Job {
    // generic
    private String jobClass;
    private int jobRuns;
    private String jobInput;
    private String jobOutput;
    private boolean printIntermediateResults;
    private String jobParams;
    private int edgeCount;
    private JobExecutionStats jobExecutionStats;
    private boolean printBenchmark;
    private boolean init;
    private String type;
    private String format;
    private String srcId;
    private int sampling;
    private int nrWalkers;
    private int rndJumpThershold;
    private float pRation;
    private float rRation;
    private int newVertices;
    private int maxID;
    private int hoops;
    private float mParam;
    private float delta;
    private int iterations;

    // hadoop specific
    private int nodes;
    private int mappers;
    private int reducers;
    private boolean delSrc;
    private boolean delIntermediate;

    // psql specific
    private String dbName;
    private String dbLogin;
    private String dbPassword;
    private String dbURL;

    // plots
    private String plotTitle;
    private String X;
    private String Y;
    private String outliers;

    public Job() { this.jobExecutionStats = new JobExecutionStats(); }

    public String getJobClass() { return jobClass; }
    public void setJobClass(String jobClass) { this.jobClass = jobClass; }

    public int getJobRuns() { return jobRuns; }
    public void setJobRuns(int jobRuns) {  this.jobRuns = jobRuns; }

    public String getJobInput() { return jobInput; }
    public void setJobInput(String jobInput) { this.jobInput = jobInput; }

    public String getJobOutput() { return jobOutput; }
    public void setJobOutput(String jobOutput) { this.jobOutput = jobOutput; }

    public boolean isPrintIntermediateResults() { return printIntermediateResults; }
    public void setPrintIntermediateResults(boolean printIntermediateResults) { this.printIntermediateResults = printIntermediateResults; }

    public String getJobParams() { return jobParams; }
    public void setJobParams(String jobParams) { this.jobParams = jobParams; }

    public int getEdgeCount() { return edgeCount; }
    public void setEdgeCount(int edgeCount) { this.edgeCount = edgeCount; }

    public String getPlotTitle() { return plotTitle; }
    public void setPlotTitle(String plotTitle) { this.plotTitle = plotTitle; }

    public JobExecutionStats getJobExecutionStats() { return jobExecutionStats; }
    public void setJobExecutionStats(JobExecutionStats jobExecutionStats) { this.jobExecutionStats = jobExecutionStats; }

    public String getX() { return X; }
    public void setX(String x) { X = x; }

    public String getY() { return Y; }
    public void setY(String y) { Y = y; }

    public boolean getPrintBenchmark() { return printBenchmark; }
    public void setPrintBenchmark(boolean printBenchmark) { this.printBenchmark = printBenchmark; }

    public boolean isInit() { return init; }
    public void setInit(boolean init) { this.init = init; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSrcId() { return srcId; }
    public void setSrcId(String srcId) { this.srcId = srcId; }

    public int getSampling() { return sampling; }
    public void setSampling(int sampling) { this.sampling = sampling; }

    public int getNrWalkers() { return nrWalkers; }
    public void setNrWalkers(int nrWalkers) { this.nrWalkers = nrWalkers; }

    public int getRndJumpThershold() { return rndJumpThershold; }
    public void setRndJumpThershold(int rndJumpThershold) { this.rndJumpThershold = rndJumpThershold; }

    public float getpRation() { return pRation; }
    public void setpRation(float pRation) { this.pRation = pRation; }

    public float getrRation() { return rRation; }
    public void setrRation(float rRation) { this.rRation = rRation; }

    public int getNewVertices() { return newVertices; }
    public void setNewVertices(int newVertices) { this.newVertices = newVertices;}

    public int getMaxID() {return maxID; }
    public void setMaxID(int maxID) { this.maxID = maxID; }

    public int getHoops() { return hoops; }
    public void setHoops(int hoops) { this.hoops = hoops; }

    public float getMParam() { return mParam; }
    public void setMParam(float mParam) { this.mParam = mParam; }

    public float getDelta() { return delta; }
    public void setDelta(float delta) { this.delta = delta; }

    public int getIterations() { return iterations; }
    public void setIterations(int iterations) { this.iterations = iterations; }

    // hadoop set/get
    public int getNodes() { return nodes; }
    public void setNodes(int nodes) { this.nodes = nodes; }

    public int getMappers() { return mappers; }
    public void setMappers(int mappers) { this.mappers = mappers; }

    public int getReducers() { return reducers; }
    public void setReducers(int reducers) { this.reducers = reducers; }

    public boolean isDelSrc() { return delSrc; }
    public void setDelSrc(boolean delSrc) { this.delSrc = delSrc; }

    public boolean isDelIntermediate() { return delIntermediate; }
    public void setDelIntermediate(boolean delIntermediate) { this.delIntermediate = delIntermediate; }

    // PSQL set/get
    public String getDbName() { return dbName; }
    public void setDbName(String dbName) { this.dbName = dbName; }

    public String getDbLogin() { return dbLogin; }
    public void setDbLogin(String dbLogin) { this.dbLogin = dbLogin; }

    public String getDbPassword() { return dbPassword; }
    public void setDbPassword(String dbPassword) { this.dbPassword = dbPassword; }

    public String getDbURL() { return dbURL; }
    public void setDbURL(String dbURL) { this.dbURL = dbURL; }

    public String getOutliers() { return outliers; }
    public void setOutliers(String outliers) { this.outliers = outliers; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}
