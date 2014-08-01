package org.testsuite.parser;

import org.jdom.Attribute;
import org.jdom.Element;
import org.testsuite.exceptions.TestSuiteException;
import org.testsuite.job.Suite;
import org.testsuite.job.utils.ConfigTags;

import java.util.Iterator;

public class OutliersParser {
    public Suite parseOutliersPlot(Element root) throws TestSuiteException {
                Suite combinedSuites = new Suite(Suite.Technology.OUTLIERS);
                Attribute plotTitleAttr = root.getAttribute(ConfigTags.combinedPlotTitle);
                if(plotTitleAttr != null) {
                    combinedSuites.setSuitePlotTitle(plotTitleAttr.getValue());
                    combinedSuites.setName(plotTitleAttr.getValue());
                    Attribute attrX = root.getAttribute(ConfigTags.X);
                    Attribute attrY = root.getAttribute(ConfigTags.Y);
                    if(attrX != null && attrY != null) {
                        // X n Y
                        combinedSuites.setX(attrX.getValue());
                        combinedSuites.setY(attrY.getValue());
                    } else
                        throw new TestSuiteException("X and Y attributes are required for <combinedPlot>");
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


