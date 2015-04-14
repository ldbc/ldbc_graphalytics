package nl.tudelft.graphalytics.graphlab.evo;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.GraphFormat;
import nl.tudelft.graphalytics.domain.algorithms.ForestFireModelParameters;
import nl.tudelft.graphalytics.graphlab.GraphLabJob;

/**
 * The job configuration of the forest-fire-model implementation for GraphLab.
 *
 * @author Jorai Rijsdijk
 */
public class ForestFireModelJob extends GraphLabJob {

    /**
     * Construct a new BreadthFirstSearchJob.
     * @param parameters The parameters for the execution of this algorithm
     * @param graphPath The path to the graph on the HDFS file system
     * @param graphFormat The format of hte graph
     */
    public ForestFireModelJob(Object parameters, String graphPath, GraphFormat graphFormat) {
        super(
                "evo/ForestFireModel.py",
                Algorithm.EVO,
                parameters,
                graphPath,
                graphFormat
        );
    }

    @Override
    public String[] formatParametersAsStrings() {
        ForestFireModelParameters evoParameters = (ForestFireModelParameters) parameters;
        return formatParametersHelper(
                String.valueOf(evoParameters.getMaxId()),
                String.valueOf(evoParameters.getPRatio()),
                String.valueOf(evoParameters.getRRatio()),
                String.valueOf(evoParameters.getMaxIterations()),
                String.valueOf(evoParameters.getNumNewVertices())
        );
    }
}
