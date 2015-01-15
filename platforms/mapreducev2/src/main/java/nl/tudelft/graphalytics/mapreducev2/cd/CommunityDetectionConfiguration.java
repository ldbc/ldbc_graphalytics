package nl.tudelft.graphalytics.mapreducev2.cd;

/**
 * Configuration constants for community detection on MapReduce version 2.
 *
 * @author Tim Hegeman
 */
public final class CommunityDetectionConfiguration {
    public static final String NODE_PREFERENCE = "CD.NodePreference";
    public static final String HOP_ATTENUATION = "CD.HopAttenuation";

    public enum LABEL_STATUS {
        STABLE,
        CHANGED
    }
}
