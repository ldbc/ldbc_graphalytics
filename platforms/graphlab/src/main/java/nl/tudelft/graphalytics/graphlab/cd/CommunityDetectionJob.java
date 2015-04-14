package nl.tudelft.graphalytics.graphlab.cd;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.GraphFormat;
import nl.tudelft.graphalytics.domain.algorithms.CommunityDetectionParameters;
import nl.tudelft.graphalytics.graphlab.GraphLabJob;

/**
 * The job configuration of the community-detection implementation for GraphLab.
 *
 * @author Jorai Rijsdijk
 */
public class CommunityDetectionJob extends GraphLabJob {

    /**
     * Construct a new BreadthFirstSearchJob.
     * @param parameters The parameters for the execution of this algorithm
     * @param graphPath The path to the graph on the HDFS file system
     * @param graphFormat The format of hte graph
     */
    public CommunityDetectionJob(Object parameters, String graphPath, GraphFormat graphFormat) {
        super(
                "cd/CommunityDetection.py",
                Algorithm.CD,
                parameters,
                graphPath,
                graphFormat
        );
    }

    @Override
    public String[] formatParametersAsStrings() {
        CommunityDetectionParameters cdParameters = (CommunityDetectionParameters) parameters;
        return formatParametersHelper(
                String.valueOf(cdParameters.getNodePreference()),
                String.valueOf(cdParameters.getHopAttenuation()),
                String.valueOf(cdParameters.getMaxIterations())
        );
    }
}
