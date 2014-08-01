package org.testsuite.job;

import java.util.ArrayList;
import java.util.List;

public class Suite {
    public enum Technology {
        NEO4J, HADOOP_V1, HADOOP_V2, GIRAPH, SQL, COMBINED_PLOT, RADAR_PLOT, COMPLEX_PLOT, OUTLIERS;
    }

    private Technology technology;
    private List<Job> suiteJobs;
    private String startMasg;
    private String suitePlotTitle;
    private String X;
    private String Y;
    private String functionName;
    private String name;
    private List<String> combinedSuites;
    private String endMsg;
    private String exe;
    private String user;
    private String password;
    private String blockSize;
    private String nodeSize;

    public Suite(Technology technology) {
        this.technology = technology;
        this.suiteJobs = new ArrayList<Job>();
        this.combinedSuites = new ArrayList<String>();
    }

    public Technology getTechnology() { return technology; }
    public void setTechnology(Technology technology) { this.technology = technology; }

    public List<Job> getSuiteJobs() { return suiteJobs; }
    public void setSuiteJobs(List<Job> suiteJobs) { this.suiteJobs = suiteJobs; }

    public String getStartMasg() { return startMasg; }
    public void setStartMasg(String startMasg) { this.startMasg = startMasg; }

    public String getEndMsg() { return endMsg; }
    public void setEndMsg(String endMsg) { this.endMsg = endMsg; }

    public void addJob(Job job) { this.getSuiteJobs().add(job); }

    public String getSuitePlotTitle() { return suitePlotTitle; }
    public void setSuitePlotTitle(String suitePlotTitle) { this.suitePlotTitle = suitePlotTitle; }

    public String getX() { return X; }
    public void setX(String x) { X = x; }

    public String getY() { return Y; }
    public void setY(String y) { Y = y; }

    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public void addCombinedSuites(String name) { this.combinedSuites.add(name); }
    public List<String> getCombinedSuites() { return combinedSuites; }
    public void setCombinedSuites(List<String> combinedSuites) { this.combinedSuites = combinedSuites; }

    public String getExe() { return exe; }
    public void setExe(String exe) { this.exe = exe; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getBlockSize() { return blockSize; }
    public void setBlockSize(String blockSize) { this.blockSize = blockSize; }

    public String getNodeSize() { return nodeSize; }
    public void setNodeSize(String nodeSize) { this.nodeSize = nodeSize; }
}
