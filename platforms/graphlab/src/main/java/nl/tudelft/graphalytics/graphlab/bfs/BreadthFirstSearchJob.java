package nl.tudelft.graphalytics.graphlab.bfs;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.GraphFormat;
import nl.tudelft.graphalytics.domain.algorithms.BreadthFirstSearchParameters;
import nl.tudelft.graphalytics.graphlab.GraphLabJob;

/**
 * The job configuration of the breadth-first-search implementation for GraphLab.
 *
 * @author Jorai Rijsdijk
 */
public class BreadthFirstSearchJob extends GraphLabJob {

    /**
     * Construct a new BreadthFirstSearchJob.
     * @param parameters The parameters for the execution of this algorithm
     * @param graphPath The path to the graph on the HDFS file system
     * @param graphFormat The format of hte graph
     */
    public BreadthFirstSearchJob(Object parameters, String graphPath, GraphFormat graphFormat) {
        super(
                "bfs/BreadthFirstSearch.py",
                Algorithm.BFS,
                parameters,
                graphPath,
                graphFormat
        );
    }

    @Override
    public String[] formatParametersAsStrings() {
        return new String[]{
                graphPath,
                graphFormat.isDirected() ? "true" : "false",
                graphFormat.isEdgeBased() ? "true" : "false",
                String.valueOf(((BreadthFirstSearchParameters) parameters).getSourceVertex())
        };
    }
}
