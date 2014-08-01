package org.testsuite.plots;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.*;
import org.jfree.date.DateUtilities;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// todo DEL
public class OutlierTEST {
  // wywalic [0] dataset dla neo4J jest znacznie wiekszy niz reszta -> rozwiazac ???
  /*private BoxAndWhiskerXYDataset createDataset() {
      final int ENTITY_COUNT = 14;

      DefaultBoxAndWhiskerXYDataset dataset = new
      DefaultBoxAndWhiskerXYDataset("Test");

      for (int i = 0; i < ENTITY_COUNT; i++) {
        Date date = DateUtilities.createDate(2003, 7, i + 1, 12, 0);
        List values = new ArrayList();
        for (int j = 0; j < 10; j++) {
            values.add(new Double(10.0 + Math.random() * 10.0));
            values.add(new Double(13.0 + Math.random() * 4.0));
        }

        dataset.add(date,
        BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(values));
      }

      return dataset;
  }*/

    private BoxAndWhiskerCategoryDataset createSampleDataset() {

        final int seriesCount = 3;
        final int categoryCount = 4;
        final int entityCount = 22;

        final DefaultBoxAndWhiskerCategoryDataset dataset
            = new DefaultBoxAndWhiskerCategoryDataset();
        for (int i = 0; i < seriesCount; i++) {
            for (int j = 0; j < categoryCount; j++) {
                final List list = new ArrayList();
                // add some values...
                for (int k = 0; k < entityCount; k++) {
                    final double value1 = 10.0 + Math.random() * 3;
                    list.add(new Double(value1));
                    final double value2 = 11.25 + Math.random(); // concentrate values in the middle
                    list.add(new Double(value2));
                }
                dataset.add(list, "Series " + i, " Type " + j);
            }

        }

        return dataset;
    }

  /*private JFreeChart createChart(final BoxAndWhiskerCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
          "Box and Whisker Chart", "Time", "Value", dataset, true);
        chart.setBackgroundPaint(new Color(249, 231, 236));

    return chart;
  }*/

    private JFreeChart createChart(final String title) {
        final BoxAndWhiskerCategoryDataset dataset = this.createSampleDataset();

        final CategoryAxis xAxis = new CategoryAxis("Type");
        final NumberAxis yAxis = new NumberAxis("Value");
        yAxis.setAutoRangeIncludesZero(false);
        final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
        renderer.setFillBox(false);
        renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
        final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

        final JFreeChart chart = new JFreeChart(
            "Box-and-Whisker Demo",
            new Font("SansSerif", Font.BOLD, 14),
            plot,
            true
        );

        return chart;
    }

  private void storeComplexPlot(JFreeChart chart) {

        try {
            //toDo !!! WAZNE -> Pathy (nie moge ciagle z hadoop wychodzic)
            File file = new File("../TestSuite/charts/outliersTEST.jpg");

            ChartUtilities.saveChartAsJPEG(file, chart, 1000, 500);
        } catch (IOException ex) {
            System.err.println("Problem occurred creating chart.");
            ex.printStackTrace();
        }
    }

  public void doMagic() {
      //BoxAndWhiskerCategoryDataset datase = this.createSampleDataset();
      JFreeChart chart = this.createChart("");
      this.storeComplexPlot(chart);
  }
}
