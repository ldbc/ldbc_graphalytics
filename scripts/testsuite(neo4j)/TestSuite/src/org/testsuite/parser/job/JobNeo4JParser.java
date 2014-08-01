package org.testsuite.parser.job;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.testsuite.exceptions.TestSuiteException;
import org.testsuite.job.Job;
import org.testsuite.job.utils.ConfigTags;

import java.util.Iterator;

public class JobNeo4JParser {
    static Logger log = Logger.getLogger(JobNeo4JParser.class.getName());

    public Job parseJob(Element root) throws TestSuiteException {
        Job job = new Job();

        // job params
        Attribute init = root.getAttribute(ConfigTags.init);
        Attribute srcId = root.getAttribute(ConfigTags.srcId);
        Attribute sampling = root.getAttribute(ConfigTags.sampling);
        Attribute walkers = root.getAttribute(ConfigTags.walkers);
        Attribute randomJump = root.getAttribute(ConfigTags.randomJump);
        Attribute pRatio = root.getAttribute(ConfigTags.pRation);
        Attribute rRatio = root.getAttribute(ConfigTags.rRation);
        Attribute newVertices = root.getAttribute(ConfigTags.newVertices);
        Attribute maxID = root.getAttribute(ConfigTags.maxID);
        Attribute hoops = root.getAttribute(ConfigTags.hoops);
        Attribute mParam = root.getAttribute(ConfigTags.mParam);
        Attribute delta = root.getAttribute(ConfigTags.delta);
        Attribute iterations = root.getAttribute(ConfigTags.iterations);
        if(init != null)
            job.setInit(new Boolean(init.getValue()));
        if(srcId != null)
            job.setSrcId(srcId.getValue());
        if(sampling != null)
            job.setSampling(Integer.valueOf(sampling.getValue()));
        if(walkers != null)
            job.setNrWalkers(Integer.valueOf(walkers.getValue()));
        if(randomJump != null)
            job.setRndJumpThershold(Integer.valueOf(randomJump.getValue()));
        // Evolution
        if(pRatio != null) {
            job.setpRation(Float.valueOf(pRatio.getValue()));
            if(pRatio != null && maxID != null) {
                job.setNewVertices(Integer.valueOf(newVertices.getValue()));
                job.setMaxID(Integer.valueOf(maxID.getValue()));
                job.setHoops(Integer.valueOf(hoops.getValue()));
            }
            else
                throw new TestSuiteException("Evolution requires at least pRation, #newVertices, maxID and hoops");
        }
        if(rRatio != null)
            job.setrRation(Float.valueOf(rRatio.getValue()));
        // Community Detection
        if(mParam != null) {
            job.setMParam(Float.valueOf(mParam.getValue()));
            if(delta != null && iterations != null) {
                job.setDelta(Float.valueOf(delta.getValue()));
                job.setIterations(Integer.valueOf(iterations.getValue()));
            }
            else
                throw new TestSuiteException("Community Detection require mParam, delta and #iterations");
        }

        Iterator<Element> elementIterator = root.getChildren().iterator();

        while (elementIterator.hasNext()) {
            Element element = elementIterator.next();

            if(element.getName().equals(ConfigTags.jobClass))
                job.setJobClass(element.getValue());
            else if(element.getName().equals(ConfigTags.jobParams))
                job.setJobParams(element.getValue());
            else if(element.getName().equals(ConfigTags.jobRuns))
                job.setJobRuns(new Integer(element.getValue()));
            else if(element.getName().equals(ConfigTags.jobDataInput)) {
                job.setJobInput(element.getValue());
                Attribute attrType = element.getAttribute(ConfigTags.type);
                Attribute attrFormat = element.getAttribute(ConfigTags.format);

                if(attrType != null && attrFormat != null) {
                    job.setType(attrType.getValue());
                    job.setFormat(attrFormat.getValue());
                }
                else
                    throw new TestSuiteException("Input type missing (directed or undirected) OR input format missing (delft or snap).");
            }
            else if(element.getName().equals(ConfigTags.jobDataOutputDir))
                job.setJobOutput(element.getValue());
            else if(element.getName().equals(ConfigTags.printIntermediateResults))
                job.setPrintIntermediateResults(new Boolean(element.getValue()));
            else if(element.getName().equals(ConfigTags.printBenchmark))
                job.setPrintBenchmark(new Boolean(element.getValue()));
            else if(element.getName().equals(ConfigTags.outliers)) {
                job.setOutliers(element.getValue());
                Attribute attrX =  element.getAttribute(ConfigTags.X);
                Attribute attrY =  element.getAttribute(ConfigTags.Y);
                if(attrX != null && attrY != null) {
                    job.setX(attrX.getValue());
                    job.setY(attrY.getValue());
                } else
                    throw new TestSuiteException("Outlier plots require both the x and y attributes");
            }
            else if(element.getName().equals(ConfigTags.plotTitle)) {
                job.setPlotTitle(element.getValue());
                Attribute attrX =  element.getAttribute(ConfigTags.X);
                if(attrX != null) {
                    job.setX(attrX.getValue());
                } else
                    throw new TestSuiteException("Job supports only X plots");
            } else
                throw new TestSuiteException("XML node not supported: "+element.getName());
        }

        if(!this.isJobValid(job))
            throw new TestSuiteException("Job configuration is NOT valid, missing one of required " +
                    "tags (<jobClass>, <jobDataInput>).");

        return job;
    }

    private boolean isJobValid(Job job) throws TestSuiteException{
        if(job.getJobClass() == null)
            return false;
        if(!job.getJobClass().equals("Stats") &&
                !job.getJobClass().equals("Detection") &&
                !job.getJobClass().equals("Evolution") &&
                !job.getJobClass().equals("BFS") &&
                !job.getJobClass().equals("Betweenness") &&
                !job.getJobClass().equals("ConnectedComponent") &&
                !job.getJobClass().equals("Sampling") &&
                !job.getJobClass().equals("FS") &&
                !job.getJobClass().equals("MRW") &&
                !job.getJobClass().equals("UniqueMRW") &&
                !job.getJobClass().equals("Random"))
            throw new TestSuiteException("Unknown job type, jobs allowed are \"Stats\", \"Detection\", \"Evolution\".");
        if(job.getJobParams() == null)
            job.setJobParams(new String());
        if(job.getJobRuns() == 0)
            job.setJobRuns(1);
        if(job.getJobInput() == null)
            return false;
        if(job.getJobOutput() == null && job.isInit() == true) //output is needed only for init jobs (input.txt, output.db)
            return false;

        return true;
    }
}
