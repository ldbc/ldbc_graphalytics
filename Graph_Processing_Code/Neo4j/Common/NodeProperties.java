package org.test.neo4j.data.utils;

public class NodeProperties {
    public static final String NODE_ID = "nodeID";
    public static final String NODE_WEIGHT = "nodeWeight";
    public static final String NODE_COMMUNITY = "nodeCommunity";
    public static final String OLD_NODE_COMMUNITY = "oldNodeCommunity";
    public static final String NODE_DEGREE = "nodeDegree";
    public static final String NODE_COMPONENT = "nodeComponent";  // also label for CommunityDetect_LPA
    public static final String NODE_COMPONENT_SCORE = "nodeComponentScore"; // label score for CommunityDetect_LPA
    public static final String NODE_DELTA_PARAM = "vertexDeltaParam"; // label score for CommunityDetect_LPA
}
