package org.testsuite.parser.job;

import org.jdom.Attribute;
import org.jdom.Element;
import org.testsuite.exceptions.TestSuiteException;
import org.testsuite.job.Job;
import org.testsuite.job.utils.ConfigTags;

import java.util.Iterator;

public class JobPsqlParser {
    public Job parseJob(Element root) throws TestSuiteException {
        Job job = new Job();

        // check if job is of init type
        Attribute init = root.getAttribute(ConfigTags.init);
        if(init != null)
            job.setInit(new Boolean(init.getValue()));

        Iterator<Element> elementIterator = root.getChildren().iterator();

        while (elementIterator.hasNext()) {
            Element element = elementIterator.next();

            if(element.getName().equals(ConfigTags.jobClass))
                job.setJobClass(element.getValue());
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
            else if(element.getName().equals(ConfigTags.dbName))
                job.setDbName(element.getValue());
            else if(element.getName().equals(ConfigTags.printBenchmark))
                job.setPrintBenchmark(new Boolean(element.getValue()));
            else if(element.getName().equals(ConfigTags.dbURL))
                job.setDbURL(element.getValue());
            else if(element.getName().equals(ConfigTags.dbLogin))
                job.setDbLogin(element.getValue());
            else if(element.getName().equals(ConfigTags.dbPass))
                job.setDbPassword(element.getValue());
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
            throw new TestSuiteException("Job configuration is NOT valid, missing one of required tags.");

        return job;
    }

    private boolean isJobValid(Job job) throws TestSuiteException{
        if(job.getJobClass() == null)
                return false;
        if(!job.getJobClass().equals("Stats") &&
                    !job.getJobClass().equals("Detection") &&
                    !job.getJobClass().equals("Evolution"))
            throw new TestSuiteException("Unknown job type, jobs allowed are \"Stats\", \"Detection\", \"Evolution\".");
        if(job.getJobRuns() == 0)
            job.setJobRuns(1);
        if(job.getDbName() == null)
            return false;
        if(job.getDbLogin() == null || job.getDbPassword() == null || job.getDbURL() == null)
            return false;
        if(job.getType() == null || job.getFormat() == null)
            return false;
        if(job.getJobInput() == null && job.isInit() == true) //output is needed only for init jobs (input.txt, output.db)
            return false;

        return true;
    }
}

