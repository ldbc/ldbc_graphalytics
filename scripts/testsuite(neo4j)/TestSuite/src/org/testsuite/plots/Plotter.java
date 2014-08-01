package org.testsuite.plots;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.testsuite.exceptions.TestSuiteException;
import org.testsuite.job.Job;
import org.testsuite.job.Suite;
import org.testsuite.job.utils.ConfigTags;
import org.testsuite.job.utils.JobExecutionStats;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Method;

public class Plotter {
    static Logger log = Logger.getLogger(Plotter.class.getName());

    public static void createPlot(List<Suite> suites) {
        log.info("Creating plots (if any) ...");

        for(Suite suite : suites) {
            Plotter.processSuite(suite);
        }

        log.info("Plots (if any) completed");
        log.info("------------------------");
    }

    public static void createCombinedPlot(List<Suite> suites) {
        Suite owner = suites.get(0);
        suites.remove(0);
        XYSeriesCollection dataset = new XYSeriesCollection();

        for(Suite suite: suites)
            dataset.addSeries(Plotter.createPlotSeriesForOwner(owner, suite));

        Plotter.storeXYPlot(owner, dataset);
    }

    public static void createRadarPlot(List<Suite> suites) {
        Suite owner = suites.get(0);
        suites.remove(0);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for(Suite suite: suites)
            Plotter.addSuiteData2RadarData(suite, dataset);

        Plotter.storeRadarPlot(owner, dataset);
    }

    public static void createComplexPlot(List<Suite> suites) {
        Suite owner = suites.get(0);
        suites.remove(0);
        List<XYPlot> plots = new ArrayList<XYPlot>(suites.size()-1); // minus owner which holds only metadata

        for(Suite suite: suites) {
            suite.setX(owner.getX());
            suite.setY(owner.getY());
            plots.add(Plotter.addSuiteData2ComplexData(suite));
        }

        CombinedDomainXYPlot complexPlot = new CombinedDomainXYPlot(new NumberAxis("Domain"));
        complexPlot.setGap(10.0);

        for(XYPlot plot : plots)
            complexPlot.add(plot, 1);

        complexPlot.setOrientation(PlotOrientation.VERTICAL);

        Plotter.storeComplexPlot(owner, complexPlot);
    }

    public static void createOutlierPlot(List<Suite> suites) {
        Suite owner = suites.get(0);
        suites.remove(0);

        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

        for(Suite suite: suites)
            Plotter.addSuiteData2OutliersData(owner, suite, dataset);

        Plotter.storeOutlierPlot(owner, dataset);
    }

    private static void processSuite(Suite suite) {
        if(suite.getSuitePlotTitle() == null) {
            for(Job job : suite.getSuiteJobs()) {
                if(job.getPlotTitle() != null)
                    Plotter.processX(job);
            }
        } else {
            Plotter.processXY(suite);
        }
    }

    private static void processX(Job job) {
        // Create a simple Bar chart
        Map<String, Method> jesMethodMap = Plotter.buildJESMethodMap();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int run = 1;
        if(job.getX().equals(ConfigTags.Time)) {
            for(Double time : job.getJobExecutionStats().getTime()) {
                dataset.setValue(time, "Time", "run_"+run);
                run++;
            }
        } else {
            if(jesMethodMap.get("get"+job.getX()) != null) {
                try{
                   double x = (Double)jesMethodMap.get("get"+job.getX()).invoke(job.getJobExecutionStats(), new Object[]{});
                    int i = 0;
                    while (i<job.getJobRuns()) {
                        dataset.setValue(x, "Time", "run_"+i);
                        i++;
                    }
                }catch (Exception ex) {ex.printStackTrace();}
            } else {
                try { throw new TestSuiteException("Unsupported benchmark variable: "+job.getX()); }
                catch (TestSuiteException ex) { ex.printStackTrace(); }
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(job.getPlotTitle(),
                    "Run number", "Time", dataset, PlotOrientation.VERTICAL,
                    false, true, false);
        try {
            ChartUtilities.saveChartAsJPEG(new File("../TestSuite/charts/"+job.getPlotTitle()+".jpg"), chart, 1000, 500);
        } catch (IOException ex) {
            System.err.println("Problem occurred creating chart.");
            ex.printStackTrace();
        }
    }

    private static void processXY(Suite suite) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(Plotter.createXYPlotSeries(suite));
        Plotter.storeXYPlot(suite, dataset);
    }

    private static XYSeries createPlotSeriesForOwner(Suite owner, Suite suite) {
        //replace X n Y
        suite.setX(owner.getX());
        suite.setY(owner.getY());

        return Plotter.createXYPlotSeries(suite);
    }

    private static XYSeries createXYPlotSeries(Suite suite) {
        XYSeries series;
        Map<String, Method> jesMethodMap = Plotter.buildJESMethodMap();

        if(suite.getFunctionName() == null)
            series = new XYSeries(suite.getX()+"/"+suite.getY());
        else
            series = new XYSeries(suite.getFunctionName());

        for(Job job : suite.getSuiteJobs()) {
            // if any plot job specific X
            if(job.getPlotTitle() != null)
                Plotter.processX(job);

            // gather X n Y from suite
            try {
                double x;
                if(suite.getX().equals(ConfigTags.Time))
                    x = ((List<Double>)jesMethodMap.get("get"+suite.getX()).invoke(job.getJobExecutionStats(), new Object[]{})).get(0);
                else
                    x = (Double)jesMethodMap.get("get"+suite.getX()).invoke(job.getJobExecutionStats(), new Object[]{});

                double y;
                if(suite.getY().equals(ConfigTags.Time))
                    y = ((List<Double>)jesMethodMap.get("get"+suite.getY()).invoke(job.getJobExecutionStats(), new Object[]{})).get(0);
                else
                    y = (Double)jesMethodMap.get("get"+suite.getY()).invoke(job.getJobExecutionStats(), new Object[]{});

                series.add(x, y);
            } catch (Exception ex) { ex.printStackTrace(); }
        }

        return series;
    }

    private static void addSuiteData2RadarData(Suite suite, DefaultCategoryDataset dataset) {
        Map<String, Method> jesMethodMap = Plotter.buildJESMethodMap();

            for(Job job : suite.getSuiteJobs()) {
                double time = job.getJobExecutionStats().getTime().get(0);
                dataset.addValue(time, suite.getTechnology().toString(), String.valueOf(job.getJobExecutionStats().getEdgeSize()));
            }
    }

    private static void addSuiteData2OutliersData(Suite owner, Suite suite, DefaultBoxAndWhiskerCategoryDataset dataset) {
        Map<String, Method> jesMethodMap = Plotter.buildJESMethodMap();
        List yAxisData = new ArrayList();

        try{
            for(Job job: suite.getSuiteJobs()) {
                if(owner.getY().equals(ConfigTags.Time)) {
                    for(Double time : job.getJobExecutionStats().getTime()) {
                        yAxisData.add(time);
                    }

                    //log.info("get"+owner.getX());
                    double x = (Double)jesMethodMap.get("get"+owner.getX()).invoke(job.getJobExecutionStats(), new Object[]{});
                    dataset.add(yAxisData, suite.getName(), Double.toString(x));
                } else {
                    log.info("Currently TSR supports only Y-time outliers");
                }
            }
        } catch (Exception ex) {ex.printStackTrace();}
    }

    private static XYPlot addSuiteData2ComplexData(Suite suite) {
        XYSeriesCollection collection = new XYSeriesCollection();
        collection.addSeries(Plotter.createXYPlotSeries(suite));
        XYItemRenderer renderer1 = new StandardXYItemRenderer();
        NumberAxis rangeAxis1 = new NumberAxis("Range 1");
        XYPlot plot = new XYPlot(collection, null, rangeAxis1, renderer1);
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

        return plot;
    }

    private static void storeXYPlot(Suite suite, XYSeriesCollection dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart(suite.getSuitePlotTitle(), suite.getX(), suite.getY(),
                    dataset, PlotOrientation.VERTICAL, true, true, false);

        //make dataset points bold
        XYPlot plot = (XYPlot) chart.getPlot();
        XYItemRenderer r = plot.getRenderer();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
        renderer.setShapesVisible(true);
        renderer.setShapesFilled(true);

        try {
            //toDo !!! WAZNE -> Pathy (nie moge ciagle z hadoop wychodzic)
            File file = new File("../TestSuite/charts/"+suite.getSuitePlotTitle()+".jpg");

            ChartUtilities.saveChartAsJPEG(file, chart, 1000, 500);
        } catch (IOException ex) {
            System.err.println("Problem occurred creating chart.");
            ex.printStackTrace();
        }
    }

    private static void storeRadarPlot(Suite suite, CategoryDataset categorydataset) {
        SpiderWebPlot spiderwebplot = new SpiderWebPlot(categorydataset);
        //spiderwebplot.setStartAngle(54D); //angle of the chart
        //spiderwebplot.setInteriorGap(0.40000000000000002D); // jest wiekszy chart jak default
        spiderwebplot.setWebFilled(true);
        spiderwebplot.setToolTipGenerator(new StandardCategoryToolTipGenerator());
        JFreeChart jfreechart = new JFreeChart(suite.getSuitePlotTitle(), TextTitle.DEFAULT_FONT, spiderwebplot, false);
        LegendTitle legendtitle = new LegendTitle(spiderwebplot);
        legendtitle.setPosition(RectangleEdge.TOP);
        jfreechart.addSubtitle(legendtitle);

        try {
            //toDo !!! WAZNE -> Pathy (nie moge ciagle z hadoop wychodzic)
            File file = new File("../TestSuite/charts/"+suite.getSuitePlotTitle()+".jpg");

            ChartUtilities.saveChartAsJPEG(file, jfreechart, 1000, 500);
        } catch (IOException ex) {
            System.err.println("Problem occurred creating chart.");
            ex.printStackTrace();
        }
    }

    private static void storeComplexPlot(Suite owner, CombinedDomainXYPlot complexPlot) {
        JFreeChart jfreechart = new JFreeChart(owner.getSuitePlotTitle(), TextTitle.DEFAULT_FONT, complexPlot, true);

        try {
            //toDo !!! WAZNE -> Pathy (nie moge ciagle z hadoop wychodzic)
            File file = new File("../TestSuite/charts/"+owner.getSuitePlotTitle()+".jpg");

            ChartUtilities.saveChartAsJPEG(file, jfreechart, 1000, 500);
        } catch (IOException ex) {
            System.err.println("Problem occurred creating chart.");
            ex.printStackTrace();
        }
    }

    private static void storeOutlierPlot(Suite owner, DefaultBoxAndWhiskerCategoryDataset dataset) {
        /*JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
          owner.getSuitePlotTitle(), owner.getX(), owner.getY(), dataset, true);
        chart.setBackgroundPaint(new Color(249, 231, 236));*/
        final CategoryAxis xAxis = new CategoryAxis(owner.getX());
        final NumberAxis yAxis = new NumberAxis(owner.getY());
        yAxis.setAutoRangeIncludesZero(false);
        final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
        renderer.setFillBox(false);
        renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
        final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

        final JFreeChart chart = new JFreeChart(owner.getSuitePlotTitle(), new Font("SansSerif", Font.BOLD, 14),
            plot, true);

        try {
            //toDo !!! WAZNE -> Pathy (nie moge ciagle z hadoop wychodzic)
            File file = new File("../TestSuite/charts/"+owner.getSuitePlotTitle()+".jpg");

            ChartUtilities.saveChartAsJPEG(file, chart, 1000, 500);
        } catch (IOException ex) {
            System.err.println("Problem occurred creating chart.");
            ex.printStackTrace();
        }
    }

    // DO NOT touch or it will bite off your head
    // method builds JobExecutionStats methods with the use of reflection
    private static Map<String, Method> buildJESMethodMap() {
        Map<String, Method> methodMap = new HashMap<String, Method>();

        Method[] methods = JobExecutionStats.class.getDeclaredMethods();
		for(Method method: methods)
            methodMap.put(method.getName(), method);

        return methodMap;
    }
}
