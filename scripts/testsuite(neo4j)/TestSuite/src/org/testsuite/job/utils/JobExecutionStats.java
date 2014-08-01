package org.testsuite.job.utils;

import java.util.ArrayList;
import java.util.List;

public class JobExecutionStats {
    //all double JFreeChart limitation
    /**
	 * @uml.property  name="time"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.Double"
	 */
    private List<Double> time;
    /**
	 * @uml.property  name="nodeSize"
	 */
    private double nodeSize; //as problem size
    /**
	 * @uml.property  name="edgeSize"
	 */
    private double edgeSize; //as problem size
    /**
	 * @uml.property  name="avgDegree"
	 */
    private double avgDegree;

    //p37 | p41 | p51 (case study 3.1)
    /**
	 * @uml.property  name="totalTime"
	 */
    private double totalTime;
    /**
	 * @uml.property  name="computationTime"
	 */
    private double computationTime;
    /**
	 * @uml.property  name="transmissionTime"
	 */
    private double transmissionTime;
    /**
	 * @uml.property  name="ioTime"
	 */
    private double ioTime;

    /**
	 * @uml.property  name="transmissionDataSize"
	 */
    private double transmissionDataSize;
    /**
	 * @uml.property  name="partitionDataSize"
	 */
    private double partitionDataSize;

    public JobExecutionStats() { this.time = new ArrayList<Double>(); }

    public List<Double> getTime() { return time; }
    public void setTime(double time) { this.time.add(time); }
    public void setTime(List<Double> time) { this.time = time; }

    /**
	 * @return
	 * @uml.property  name="nodeSize"
	 */
    public double getNodeSize() { return nodeSize; }
    /**
	 * @param nodeSize
	 * @uml.property  name="nodeSize"
	 */
    public void setNodeSize(double nodeSize) { this.nodeSize = nodeSize; }

    /**
	 * @return
	 * @uml.property  name="edgeSize"
	 */
    public double getEdgeSize() { return edgeSize; }
    /**
	 * @param edgeSize
	 * @uml.property  name="edgeSize"
	 */
    public void setEdgeSize(double edgeSize) { this.edgeSize = edgeSize; }
}
