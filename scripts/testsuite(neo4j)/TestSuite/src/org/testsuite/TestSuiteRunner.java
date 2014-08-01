package org.testsuite;

import org.jdom.JDOMException;
import org.testsuite.exceptions.TestSuiteException;
import org.testsuite.job.Suite;
import org.testsuite.parser.TestSuiteConfigParser;
import org.testsuite.plots.OutlierTEST;
import org.testsuite.plots.Plotter;
import org.testsuite.tool.SuiteTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class TestSuiteRunner {
    static Logger log = Logger.getLogger(TestSuiteRunner.class.getName());

    private static class CombinedPlotsHelper {
        public static void createPlot(Map<Suite.Technology, List<Suite>> technologySuites, Suite.Technology technology) {
            Iterator<Suite> combinedPlotsIter = technologySuites.get(technology).iterator();
            while (combinedPlotsIter.hasNext()){
                Suite combinedPlot = combinedPlotsIter.next();
                List<Suite> combinedPlotsSuites = new ArrayList<Suite>();
                combinedPlotsSuites.add(combinedPlot); // first elem holds plot metadata
                List<String> suiteNames = combinedPlot.getCombinedSuites();
                // gather suites
                CombinedPlotsHelper.gatherSuites(technologySuites, suiteNames, combinedPlotsSuites);

                // create plots
                switch (technology) {
                        case COMPLEX_PLOT:
                            Plotter.createComplexPlot(combinedPlotsSuites);
                            break;

                        case COMBINED_PLOT:
                            Plotter.createCombinedPlot(combinedPlotsSuites);
                            break;

                        case RADAR_PLOT:
                            Plotter.createRadarPlot(combinedPlotsSuites);
                            break;

                        case OUTLIERS:
                            Plotter.createOutlierPlot(combinedPlotsSuites);
                            break;

                        default:
                            break;
                }
            }
        }

        private static void gatherSuites(Map<Suite.Technology, List<Suite>> technologySuites, List<String> suiteNames, List<Suite> combinedPlotsSuites) {
            // toDo this is crap
            for(List<Suite> suites : technologySuites.values()) {
                for(Suite suite : suites) {
                    if(suite.getName() != null) {
                        for(String name : suiteNames) {
                            if(name.equals(suite.getName()))
                                combinedPlotsSuites.add(suite);
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, JDOMException, TestSuiteException{
        String config = args[0];
        TestSuiteConfigParser parser = new TestSuiteConfigParser();
        Map<Suite.Technology, List<Suite>> technologySuites =  parser.parseConfig(config);
        Iterator<Suite.Technology> technologyIterator = technologySuites.keySet().iterator();

        while (technologyIterator.hasNext()) {
            Suite.Technology technology = technologyIterator.next();
            List<Suite> returnSuites;
            switch (technology) {
                case NEO4J:
                    returnSuites = new SuiteTool().runTestSuites(technologySuites.get(technology));
                    Plotter.createPlot(returnSuites);
                    break;

                case HADOOP_V1:
                    returnSuites = new SuiteTool().runTestSuites(technologySuites.get(technology));
                    //Plotter.createPlot(returnSuites); // FINISH for scripts execution
                    break;

                case HADOOP_V2:
                    returnSuites = new SuiteTool().runTestSuites(technologySuites.get(technology));
                    //Plotter.createPlot(returnSuites); // FINISH for scripts execution
                    break;

                case GIRAPH:
                    //todo
                    break;

                case SQL:
                    returnSuites = new SuiteTool().runTestSuites(technologySuites.get(technology));
                    Plotter.createPlot(returnSuites);
                    break;

                default:
                    break;
            }
        }

        /*
            TODO this will fail due to Hadoop && Giraph asynchronous script execution FIX IT
         */
        System.out.println("!!! TODO !!! FIND a way to shut down all plots until scripts finish their execution ELSE TSR will fail on plots");

        /*
            Complex plots generation
         */

        if(technologySuites.get(Suite.Technology.COMPLEX_PLOT) == null) {
            log.debug("No complex plots defined.");
        } else {
            //gather radar plot
            log.info("Creating complex plots ...");
            CombinedPlotsHelper.createPlot(technologySuites, Suite.Technology.COMPLEX_PLOT);
            log.info("Complex plot completed");
        }

        if(technologySuites.get(Suite.Technology.RADAR_PLOT) == null) {
            log.debug("No radar plots defined.");
        } else {
            //gather radar plot
            log.info("Creating radar plots ...");
            CombinedPlotsHelper.createPlot(technologySuites, Suite.Technology.RADAR_PLOT);
            log.info("Radar plots completed");
        }

        if(technologySuites.get(Suite.Technology.OUTLIERS) == null) {
            log.debug("No outliers plots defined.");
        } else {
            //gather radar plot
            log.info("Creating outliers plots ...");
            CombinedPlotsHelper.createPlot(technologySuites, Suite.Technology.OUTLIERS);
            log.info("Outliers plots completed");
        }

        if(technologySuites.get(Suite.Technology.COMBINED_PLOT) == null) {
            log.debug("No combined plots defined.");
            log.info("THE END :)");
            log.debug("############# END OF TESTs #############"); // to mark different TSR runs
            return;
        } else {
            log.info("Creating combined plots ...");
            CombinedPlotsHelper.createPlot(technologySuites, Suite.Technology.COMBINED_PLOT);
            log.info("Combined plots completed");
        }

        log.info("---------------------------------");
        log.info("THE END :)");
        log.debug("################### END OF TESTs ###################"); // to mark different TSR runs
    }
}
