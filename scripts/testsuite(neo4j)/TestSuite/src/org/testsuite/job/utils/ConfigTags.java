package org.testsuite.job.utils;

public class ConfigTags {
    // platforms
    public static final String Neo4J = "Neo4J";
    public static final String HADOOP = "Hadoop";
    public static final String Giraph = "Giraph";
    public static final String PSQL = "PSQL";
    
    // suite config
    public static final String suite = "suite";
    public static final String suiteStartMsg = "suiteStartMsg";
    public static final String suiteEndMsg = "suiteEndMsg";
    public static final String suitePlotTitle = "suitePlotTitle";
    public static final String suiteName = "name";
    // DAS execution
    public static final String suiteExe = "exe";
    public static final String suiteUser = "user";
    public static final String suitePassword = "password";
    public static final String blockSize = "blockSize";
    public static final String nodesSize = "nodesSize";
    
    // job config
    public static final String job = "job";
    public static final String jobClass = "jobClass";
    public static final String jobRuns = "jobRuns";
    public static final String jobDataInput = "jobDataInput";
    public static final String jobDataOutputDir = "jobDataOutputDir";
    public static final String printIntermediateResults = "printIntermediateResults";
    public static final String printBenchmark = "printBenchmark";
    public static final String jobParams = "jobParams";
    public static final String init = "init";
    public static final String edgeCount = "edgeCount";
    public static final String type = "type";
    public static final String format = "format";
    public static final String srcId = "srcId";

    // job parameters
    public static final String sampling = "sampling";       // sampling (sample size)
    public static final String walkers = "walkers";         // sampling (not used)
    public static final String randomJump = "randomJump";   // sampling UMRW
    public static final String pRation = "pRation";         // evolution
    public static final String rRation = "rRation";         // evolution
    public static final String newVertices = "newVertices"; // evolution
    public static final String maxID = "maxID";             // evolution
    public static final String hoops = "hoops";             // evolution
    public static final String mParam = "mParam";           // community
    public static final String delta = "delta";             // community
    public static final String iterations = "iterations";   // community
    
    //plot config
    public static final String X = "x";
    public static final String Y = "y";
    public static final String functionName = "functionName";
    public static final String plotTitle = "plotTitle";
    public static final String outliers = "outliers";
    // combined  and radar plot
    public static final String combinedPlot = "combinedPlot";
    public static final String radarPlot = "radarPlot";
    public static final String complexPlot = "complexPlot";
    public static final String combinedPlotTitle = "title";

    // benchmark values
    public static final String Time = "Time"; // special, the only List<Double>, checked in plotter for different processing

    // hadooop configs
    public static final String version = "version";
    public static final String mappers = "mappers";
    public static final String reducers = "reducers";
    public static final String nodes = "nodes";
    public static final String delSrc="delSrc";
    public static final String delIntermediate="delIntermediate";

    // psql configs
    public static final String dbName = "dbName";
    public static final String dbLogin = "dbLogin";
    public static final String dbPass = "dbPass";
    public static final String dbURL = "dbURL";
}
