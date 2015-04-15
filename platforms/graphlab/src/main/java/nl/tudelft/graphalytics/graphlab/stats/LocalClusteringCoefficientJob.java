package nl.tudelft.graphalytics.graphlab.stats;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.GraphFormat;
import nl.tudelft.graphalytics.graphlab.GraphLabJob;

/**
 * The job configuration of the local-clustering-coefficient implementation for GraphLab.
 *
 * @author Jorai Rijsdijk
 */
public class LocalClusteringCoefficientJob extends GraphLabJob {

    /**
     * Construct a new BreadthFirstSearchJob.
     * @param graphPath The path to the graph on the HDFS file system
     * @param graphFormat The format of hte graph
     */
    public LocalClusteringCoefficientJob(String graphPath, GraphFormat graphFormat) {
        super(
                "stats/LocalClusteringCoefficient.py",
                Algorithm.STATS,
                null,
                graphPath,
                graphFormat
        );
    }

    @Override
    public String[] formatParametersAsStrings() {
        return formatParametersHelper();
    }
}
