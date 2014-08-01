package org.testsuite.parser.job;

import org.jdom.Attribute;
import org.jdom.Element;
import org.testsuite.exceptions.TestSuiteException;
import org.testsuite.job.Job;
import org.testsuite.job.utils.ConfigTags;

import java.util.Iterator;

public class JobHadoopPraser {
    public Job parseJob(Element root) throws TestSuiteException {
        Job job = new Job();
        // check if job is of init type
        Attribute init = root.getAttribute(ConfigTags.init);
        if(init != null)
            job.setInit(new Boolean(init.getValue()));
        // check intermidiate results policy
        Attribute attrDelSrc = root.getAttribute(ConfigTags.delSrc);
        Attribute attrDelIntermediate = root.getAttribute(ConfigTags.delIntermediate);
        if(attrDelSrc != null)
            job.setDelSrc(Boolean.parseBoolean(attrDelSrc.getValue()));
        if(attrDelIntermediate != null)
            job.setDelIntermediate(Boolean.parseBoolean(attrDelIntermediate.getValue()));

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
                    throw new TestSuiteException("Input type missing (directed or undirected) OR input format missing (delft or snap) OR path detail missing (local := boolean).");
            }
            else if(element.getName().equals(ConfigTags.jobDataOutputDir))
                job.setJobOutput(element.getValue());
            else if(element.getName().equals(ConfigTags.printIntermediateResults))
                job.setPrintIntermediateResults(new Boolean(element.getValue()));
            else if(element.getName().equals(ConfigTags.printBenchmark))
                job.setPrintBenchmark(new Boolean(element.getValue()));
            else if(element.getName().equals(ConfigTags.edgeCount))
                job.setEdgeCount(new Integer(element.getValue()));
            else if(element.getName().equals(ConfigTags.nodes))
                job.setNodes(new Integer(element.getValue()));
            else if(element.getName().equals(ConfigTags.mappers))
                job.setMappers(new Integer(element.getValue()));
            else if(element.getName().equals(ConfigTags.reducers))
                job.setReducers(new Integer(element.getValue()));
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
            throw new TestSuiteException("Job configuration is NOT valid, missing required " +
                "tags (one of <jobClass>, <jobDataInput>, <jobDataOutputDir>).");

        return job;
    }

    //todo test nodes division [4, 5, 9, 12]
    // validates Job and setts up the nodes division for mappers n reducers
    private boolean isJobValid(Job job) throws TestSuiteException{
        if(job.getJobClass() == null)
            return false;
        if(!job.getJobClass().equals("Stats") &&
                !job.getJobClass().equals("Detection") &&
                !job.getJobClass().equals("Evolution"))
            throw new TestSuiteException("Unknown job type, jobs allowed are \"Stats\", \"Detection\", \"Evolution\".");
        if(job.getJobParams() == null)
            job.setJobParams(new String());
        if(job.getJobRuns() == 0)
            job.setJobRuns(1);
        if(job.getJobInput() == null)
            return false;
        if(job.getJobOutput() == null)
            return false;

        // setting up nodes division
        if(job.getMappers() > 0 && job.getReducers() > 0)
            return true;
        else if(job.getNodes() == 0)
            throw new TestSuiteException("Hadoop config requires <nodes> tag to be defined");
        else if(job.getMappers() > 0)
            job.setReducers(job.getNodes() - job.getMappers());
        else if(job.getReducers() > 0)
            job.setMappers(job.getNodes() - job.getReducers());
        else if(job.getNodes() == 1) {
            job.setMappers(1);
            job.setReducers(1);
        } else if(job.getNodes() > 1) {
            int nodes = job.getNodes();
            float quarter = (float) nodes / (float)4;
            int reducers = Math.round(quarter);
            job.setMappers(reducers * 3);
            job.setReducers(reducers);
        } else
            throw new TestSuiteException("Hadoop mappers/reducer configuration has gone horribly wrong ;]");

        return true;
    }
}
