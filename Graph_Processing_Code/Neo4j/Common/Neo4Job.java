package org.test.neo4j.utils;

public class Neo4Job {
    private String jobClass;
    private int jobRuns;
    private String jobInput;
    private String jobOutput;
    private boolean printIntermediateResults;
    private String jobParams;
    private int edgeCount;
    private Neo4JobExecutionStats jobExecutionStats;
    private boolean printBenchmark;
    private boolean init;
    private String type;
    private String format;
    private String srcID; // for BFS algorithm
    private int samplingBudget;
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
    // custom for shell scripts
    private int runID;
    private String logDIR;

    public String getJobClass() { return jobClass; }
    public void setJobClass(String jobClass) { this.jobClass = jobClass; }

    public int getJobRuns() { return jobRuns; }
    public void setJobRuns(int jobRuns) { this.jobRuns = jobRuns; }

    public String getJobInput() { return jobInput; }
    public void setJobInput(String jobInput) { this.jobInput = jobInput; }

    public String getJobOutput() { return jobOutput; }
    public void setJobOutput(String jobOutput) { this.jobOutput = jobOutput; }

    public boolean getPrintIntermediateResults() { return printIntermediateResults; }
    public void setPrintIntermediateResults(boolean printIntermediateResults) { this.printIntermediateResults = printIntermediateResults;}

    public String getJobParams() { return jobParams; }
    public void setJobParams(String jobParams) { this.jobParams = jobParams; }

    public int getEdgeCount() { return edgeCount; }
    public void setEdgeCount(int edgeCount) { this.edgeCount = edgeCount; }

    public Neo4JobExecutionStats getJobExecutionStats() { return jobExecutionStats; }
    public void setJobExecutionStats(Neo4JobExecutionStats jobExecutionStats) { this.jobExecutionStats = jobExecutionStats; }

    public boolean getPrintBenchmark() { return printBenchmark; }
    public void setPrintBenchmark(boolean printBenchmark) { this.printBenchmark = printBenchmark; }

    public boolean isInit() { return init; }
    public void setInit(boolean init) { this.init = init; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getSrcID() { return srcID; }
    public void setSrcID(String srcID) { this.srcID = srcID; }

    public int getSamplingBudget() { return samplingBudget;}
    public void setSamplingBudget(int samplingBudget) { this.samplingBudget = samplingBudget; }

    public int getNrWalkers() {return nrWalkers;}
    public void setNrWalkers(int nrWalkers) {this.nrWalkers = nrWalkers;}

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

    public int getRunID() { return runID; }
    public void setRunID(int runID) { this.runID = runID; }

    public String getLogDIR() { return logDIR; }
    public void setLogDIR(String logDIR) { this.logDIR = logDIR; }
}
