package org.testsuite.parser;


import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.testsuite.exceptions.TestSuiteException;
import org.testsuite.job.Suite;
import org.testsuite.job.utils.ConfigTags;
import org.testsuite.parser.job.JobHadoopPraser;
import org.testsuite.parser.job.JobNeo4JParser;
import org.testsuite.parser.job.JobPsqlParser;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TestSuiteConfigParser {
    static Logger log = Logger.getLogger(TestSuiteConfigParser.class.getName());

    public Map<Suite.Technology, List<Suite>> parseConfig(String config) throws IOException, JDOMException, TestSuiteException {
        log.debug("Processing configuration file \""+config+"\"");
        Map<Suite.Technology, List<Suite>> suitesMap = new HashMap<Suite.Technology, List<Suite>>();

        SAXBuilder builder = new SAXBuilder();
	    File xmlFile = new File(config);
        Document document = (Document) builder.build(xmlFile);
		Element rootNode = document.getRootElement();
        Iterator<Element> technologySuitesIterator = rootNode.getChildren().iterator();

        while (technologySuitesIterator.hasNext()) {
            Element element = technologySuitesIterator.next();

            //CombinedPlots
            if(element.getName().equals(ConfigTags.combinedPlot)) {
                List<Suite> suites = suitesMap.get(Suite.Technology.COMBINED_PLOT);
                // create
                if(suites == null) {
                    suites = new ArrayList<Suite>();
                    suites.add(new CombinedPlotParser().parseCombinedPlot(element));
                    suitesMap.put(Suite.Technology.COMBINED_PLOT, suites);
                }
                // append
                else {
                    suites.add(new CombinedPlotParser().parseCombinedPlot(element));
                    suitesMap.put(Suite.Technology.COMBINED_PLOT, suites);
                }
            } // RadarPlot
            else if(element.getName().equals(ConfigTags.radarPlot)) {
                List<Suite> suites = suitesMap.get(Suite.Technology.RADAR_PLOT);
                // create
                if(suites == null) {
                    suites = new ArrayList<Suite>();
                    suites.add(new RadarPlotParser().parseCombinedPlot(element));
                    suitesMap.put(Suite.Technology.RADAR_PLOT, suites);
                }
                // append
                else {
                    suites.add(new CombinedPlotParser().parseCombinedPlot(element));
                    suitesMap.put(Suite.Technology.RADAR_PLOT, suites);
                }
            }// ComplexPlot
            else if(element.getName().equals(ConfigTags.complexPlot)) {
                List<Suite> suites = suitesMap.get(Suite.Technology.COMPLEX_PLOT);
                // create
                if(suites == null) {
                    suites = new ArrayList<Suite>();
                    suites.add(new ComplexPlotParser().parseComplexPlot(element));
                    suitesMap.put(Suite.Technology.COMPLEX_PLOT, suites);
                }
                // append
                else {
                    suites.add(new ComplexPlotParser().parseComplexPlot(element));
                    suitesMap.put(Suite.Technology.COMPLEX_PLOT, suites);
                }
            }// Outliers
            else if(element.getName().equals(ConfigTags.outliers)) {
                List<Suite> suites = suitesMap.get(Suite.Technology.OUTLIERS);
                // create
                if(suites == null) {
                    suites = new ArrayList<Suite>();
                    suites.add(new OutliersParser().parseOutliersPlot(element));
                    suitesMap.put(Suite.Technology.OUTLIERS, suites);
                }
                // append
                else {
                    suites.add(new OutliersParser().parseOutliersPlot(element));
                    suitesMap.put(Suite.Technology.OUTLIERS, suites);
                }
            }// Neo4J
            else if(element.getName().equals(ConfigTags.Neo4J)){
                List<Suite> suites = suitesMap.get(Suite.Technology.NEO4J);
                // create
                if(suites == null)
                    suitesMap.put(Suite.Technology.NEO4J, this.parseSuites(Suite.Technology.NEO4J, element));
                // append
                else {
                    suites.addAll(this.parseSuites(Suite.Technology.NEO4J, element));
                    suitesMap.put(Suite.Technology.NEO4J, suites);
                }
            }// Hadoop
            else if(element.getName().equals(ConfigTags.HADOOP)){
                // check hadoop version
                Attribute versionAttr = element.getAttribute(ConfigTags.version);
                if(versionAttr != null) {
                    if(versionAttr.getValue().equals("1")) {
                        List<Suite> suites = suitesMap.get(Suite.Technology.HADOOP_V1);
                        // create
                        if(suites == null)
                            suitesMap.put(Suite.Technology.HADOOP_V1, this.parseSuites(Suite.Technology.HADOOP_V1, element));
                        // append
                        else {
                            suites.addAll(this.parseSuites(Suite.Technology.HADOOP_V1, element));
                            suitesMap.put(Suite.Technology.HADOOP_V1, suites);
                        }
                    } else if(versionAttr.getValue().equals("2")) {
                        List<Suite> suites = suitesMap.get(Suite.Technology.HADOOP_V2);
                        // create
                        if(suites == null)
                            suitesMap.put(Suite.Technology.HADOOP_V2, this.parseSuites(Suite.Technology.HADOOP_V2, element));
                        // append
                        else {
                            suites.addAll(this.parseSuites(Suite.Technology.HADOOP_V2, element));
                            suitesMap.put(Suite.Technology.HADOOP_V2, suites);
                        }
                    } else
                        throw new TestSuiteException("Invalid version attribute value, allowed only 1 or 2.");
                } else
                    throw new TestSuiteException("Hadoop config requires version attribute.");
            } // PSQL
            else if(element.getName().equals(ConfigTags.PSQL)){
                List<Suite> suites = suitesMap.get(Suite.Technology.SQL);
                // create
                if(suites == null)
                    suitesMap.put(Suite.Technology.SQL, this.parseSuites(Suite.Technology.SQL, element));
                // append
                else {
                    suites.addAll(this.parseSuites(Suite.Technology.SQL, element));
                    suitesMap.put(Suite.Technology.SQL, suites);
                }
            }
        }

        log.info("Processing configuration file COMPLETED");
        log.info("---------------------------------------");
        return suitesMap;
    }

    private List<Suite> parseSuites(Suite.Technology technology, Element root) throws TestSuiteException {
        List<Suite> suites = new ArrayList<Suite>();
        Iterator<Element> jobsIterator = root.getChildren().iterator();
        while (jobsIterator.hasNext()) {
            suites.add(this.parseSuit(technology, jobsIterator.next()));
        }

        return suites;
    }


    private Suite parseSuit(Suite.Technology technology, Element root) throws TestSuiteException {
        Suite suite = new Suite(technology);
        // execution local || das4 ONLY FOR cloud platforms
        if(root.getParentElement().getName().equals(ConfigTags.HADOOP) ||
           root.getParentElement().getName().equals(ConfigTags.Giraph)) {
            Attribute suiteExe = root.getAttribute(ConfigTags.suiteExe);
            Attribute suiteUser = root.getAttribute(ConfigTags.suiteUser);
            Attribute suitePassword = root.getAttribute(ConfigTags.suitePassword);
            Attribute blockSize = root.getAttribute(ConfigTags.blockSize);
            Attribute nodesSize = root.getAttribute(ConfigTags.nodesSize);
            if(suiteExe != null) {
                suite.setExe(suiteExe.getValue());
                if(suiteUser != null && suitePassword != null) {
                    suite.setUser(suiteUser.getValue());
                    suite.setPassword(suitePassword.getValue());
                    if(blockSize != null)
                        suite.setBlockSize(blockSize.getValue());
                    if(nodesSize != null)
                        suite.setNodeSize(nodesSize.getValue());
                }

                //validate
                if("das".equals(suiteExe) && (suiteUser == null || suitePassword == null))
                    throw new TestSuiteException("User name or password not provided. For Das execution they are required.");
            }
        }
        // suite name <-used for combined plots
        Attribute suiteName = root.getAttribute(ConfigTags.suiteName);
            if(suiteName != null)
                suite.setName(suiteName.getValue());
        // build basic suite metadata
        if(root.getAttribute(ConfigTags.suitePlotTitle) != null) {
            suite.setSuitePlotTitle(root.getAttribute(ConfigTags.suitePlotTitle).getValue());
            Attribute attrX = root.getAttribute(ConfigTags.X);
            Attribute attrY = root.getAttribute(ConfigTags.Y);
            if(attrX != null && attrY != null) {
                // X n Y
                suite.setX(attrX.getValue());
                suite.setY(attrY.getValue());
                // function name
                Attribute functionName = root.getAttribute(ConfigTags.functionName);
                if(functionName != null)
                    suite.setFunctionName(functionName.getValue());
            } else
                throw new TestSuiteException("Unsupported suite plot");
        } else if(root.getAttribute(ConfigTags.functionName) != null)
            suite.setFunctionName(root.getAttribute(ConfigTags.functionName).getValue());

        // suite jobs
        Iterator<Element> elementIterator = root.getChildren().iterator();
        while (elementIterator.hasNext()) {
            Element element = elementIterator.next();
            if(element.getName().equals(ConfigTags.suiteStartMsg))
                suite.setStartMasg(element.getValue());
            else if(element.getName().equals(ConfigTags.suiteEndMsg))
                suite.setEndMsg(element.getValue());
            else if(element.getName().equals(ConfigTags.job)) {
                switch (suite.getTechnology()) {
                    case NEO4J:
                        suite.addJob(new JobNeo4JParser().parseJob(element));
                        break;

                    case HADOOP_V1:
                        suite.addJob(new JobHadoopPraser().parseJob(element));
                        break;

                    case HADOOP_V2:
                        suite.addJob(new JobHadoopPraser().parseJob(element));
                        break;

                    case GIRAPH:
                        //todo GIRAPH_Job_Parser
                        break;

                    case SQL:
                        suite.addJob(new JobPsqlParser().parseJob(element));
                        break;

                    default:
                        throw new TestSuiteException("All your base belongs to me :) | Suite.Technology unknown.");
                }
            }
            else
                throw new TestSuiteException("XML node not supported: "+element.getName());
        }

        return suite;
    }
}
