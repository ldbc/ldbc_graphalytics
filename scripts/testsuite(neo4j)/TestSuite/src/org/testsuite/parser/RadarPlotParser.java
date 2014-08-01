package org.testsuite.parser;

import org.jdom.Attribute;
import org.jdom.Element;
import org.testsuite.exceptions.TestSuiteException;
import org.testsuite.job.Suite;
import org.testsuite.job.utils.ConfigTags;

import java.util.Iterator;

public class RadarPlotParser {
    public Suite parseCombinedPlot(Element root) throws TestSuiteException {
            Suite combinedSuites = new Suite(Suite.Technology.COMBINED_PLOT);
            Attribute plotTitleAttr = root.getAttribute(ConfigTags.combinedPlotTitle);
            if(plotTitleAttr != null) {
                combinedSuites.setSuitePlotTitle(plotTitleAttr.getValue());
                combinedSuites.setName(plotTitleAttr.getValue());
            } else
                throw new TestSuiteException("<combinedPlot> requires attribute \"title\"");

            Iterator<Element> suites = root.getChildren().iterator();
            while (suites.hasNext()) {
                Element plotSuit = suites.next();
                if(plotSuit.getName().equals(ConfigTags.suite)){
                    Attribute suiteName = plotSuit.getAttribute(ConfigTags.suiteName);
                    if(suiteName != null) {
                        combinedSuites.addCombinedSuites(suiteName.getValue());
                    } else
                        throw new TestSuiteException("<suite> tag within <combinedPlot> tag requires name attribute.");
                } else
                    throw new TestSuiteException("<combinedPlot> allows only <suite name=*> tags.");

            }

            return combinedSuites;
        }
    }
