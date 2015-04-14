package nl.tudelft.graphalytics.graphlab.conn;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.GraphFormat;
import nl.tudelft.graphalytics.graphlab.GraphLabJob;

/**
 * The job configuration of the connected-components implementation for GraphLab.
 * @author Jorai Rijsdijk
 */
public class ConnectedComponentsJob extends GraphLabJob {

    /**
     * Construct a new BreadthFirstSearchJob.
     * @param graphPath   The path to the graph on the HDFS file system
     * @param graphFormat The format of hte graph
     */
    public ConnectedComponentsJob(String graphPath, GraphFormat graphFormat) {
        super(
                "conn/ConnectedComponents.py",
                Algorithm.CONN,
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
