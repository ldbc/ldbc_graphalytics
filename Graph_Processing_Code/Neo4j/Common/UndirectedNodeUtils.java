package org.test.neo4j.data.utils;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Traverser;
import org.test.neo4j.data.GPNode;
import org.test.neo4j.data.Triad;

import java.util.*;

public class UndirectedNodeUtils {
    private static Map<Float, Integer> oldCommunityTotalDegree = new HashMap<Float, Integer>();
    private static Map<Float, Integer> communityTotalDegree = new HashMap<Float, Integer>();
    static Logger log = Logger.getLogger(UndirectedNodeUtils.class.getName());

    public static List<Node> getNodeNeighbourhood(Node centralNode) {
        List<Node> nodeNeighbours = new ArrayList<Node>();
        // collect neighbours
        Iterator<Relationship> centralEdgeIter = centralNode.getRelationships(Direction.OUTGOING).iterator();
        while (centralEdgeIter.hasNext()) {
            Relationship rel = centralEdgeIter.next();
            nodeNeighbours.add(rel.getOtherNode(centralNode));
        }

        return nodeNeighbours;
    }

    public static List<Relationship> collectNeighboursEdges(Node centralNode) {
        List<Relationship> neighboursEdges = new ArrayList<Relationship>();
        Iterator<Node> neighboursIter = UndirectedNodeUtils.getNodeNeighbourhood(centralNode).iterator();
        while (neighboursIter.hasNext()) {
            Node neighbour = neighboursIter.next();
            Iterator<Relationship> neighbourEdges = neighbour.getRelationships(Direction.OUTGOING).iterator();
            while (neighbourEdges.hasNext()) {
                neighboursEdges.add(neighbourEdges.next());
            }
        }

        return neighboursEdges;
    }

    // long workaround due to limitations of creating Relationship
    // can't simply create tmpRelationship, this would modify node
    public static List<Triad> getNodeTriads(Node centralNode) {
        List<Triad> result = new ArrayList<Triad>();
        Iterator<Relationship> centralNodeEdgesIter = centralNode.getRelationships(Direction.OUTGOING).iterator();
        List<Relationship> neighboursEdges = UndirectedNodeUtils.collectNeighboursEdges(centralNode);

        // A -> B -> C -> A
        while (centralNodeEdgesIter.hasNext()) {
            Relationship centralEdge = centralNodeEdgesIter.next();
            int centralDst = (Integer)centralEdge.getEndNode().getProperty(NodeProperties.NODE_ID);
            for(int i=0; i<neighboursEdges.size(); i++) {
                Relationship neighbourEdge = neighboursEdges.get(i);
                // A -> B
                if((Integer)neighbourEdge.getStartNode().getProperty(NodeProperties.NODE_ID) == centralDst) {
                    for(int j=0; j<neighboursEdges.size(); j++) {
                        Relationship neighbourEdge2 = neighboursEdges.get(j);
                        // B -> C && C -> A
                        if(neighbourEdge2.getStartNode().getProperty(NodeProperties.NODE_ID)
                                == neighbourEdge.getEndNode().getProperty(NodeProperties.NODE_ID) &&
                                neighbourEdge2.getEndNode().getProperty(NodeProperties.NODE_ID) == centralNode.getProperty(NodeProperties.NODE_ID)) {
                            // check if triad is already present in results
                            Triad candidate = new Triad(centralEdge, neighbourEdge, neighbourEdge2);
                            TriadHelper helper = new TriadHelper();
                            if(!helper.exists(candidate, result)) {
                                result.add(new Triad(centralEdge, neighbourEdge, neighbourEdge2));
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    public static float calculateNodeCC(Node centralNode) {
        List<Node> nodeNeighbours;

        // collect neighbours
        nodeNeighbours = UndirectedNodeUtils.getNodeNeighbourhood(centralNode);

        // process edges
        Map<String, Boolean> centralNeighboursIds = UndirectedNodeUtils.buildNeighboursMap(centralNode);
        int counter = 0;
        for(Node neighbour : nodeNeighbours) {
            Iterator<Relationship> outEdges =  neighbour.getRelationships(Direction.OUTGOING).iterator();
            while(outEdges.hasNext()) {
                Relationship outEdge = outEdges.next();
                //comparing only dst; src is known to be a neighbour
                if(centralNeighboursIds.get((String)outEdge.getEndNode().getProperty(NodeProperties.NODE_ID)) != null) {
                    counter++;
                }
            }
        }

        float bottom = (nodeNeighbours.size() * (nodeNeighbours.size() - 1));

        if(bottom == 0)
            return 0;

        return counter/bottom;
    }

    private static Map<String, Boolean> buildNeighboursMap(Node node) {
        Map<String, Boolean> centralNeighboursIds = new HashMap<String, Boolean>();

        for(Relationship edge : node.getRelationships(Direction.OUTGOING, RelTypes.NodeRelation.KNOWS)) {
            centralNeighboursIds.put((String)edge.getEndNode().getProperty(NodeProperties.NODE_ID), true);
        }

        return centralNeighboursIds;
    }

    public static void calculateNodeRelationshipsWeights(Node node, GraphDatabaseService graphDb) {
        List<Triad> triads = UndirectedNodeUtils.getNodeTriads(node);
        Iterator<Relationship> nodeEdgesIter = node.getRelationships(Direction.OUTGOING).iterator();
        int nodeDegree = 0;

        while (nodeEdgesIter.hasNext()) {
            nodeDegree++;
            // count in how many triads does tmpEdge occurs
            Relationship tmpEdge = nodeEdgesIter.next();
            Iterator<Triad> triadIterator = triads.iterator();
            int counter = 0;

            while (triadIterator.hasNext()) {
                Triad tmpTriad = triadIterator.next();
                if(tmpTriad.isMember(tmpEdge))
                    counter++;
            }

            // count in how many triads all node edges occur
            int bottom = 0;
            Iterator<Relationship> nodeEdgesIter2 = node.getRelationships(Direction.OUTGOING).iterator();
            while (nodeEdgesIter2.hasNext()) {
                Relationship rel = nodeEdgesIter2.next();
                Iterator<Triad> triadsIter = triads.iterator();
                while (triadsIter.hasNext()) {
                    Triad tmpTriad = triadsIter.next();
                    if(tmpTriad.isMember(rel))
                        bottom++;
                }
            }


            float weight = (float )counter / (float)bottom;

            if(bottom == 0)
                weight = 1;

            // commit new property
            Transaction tx = graphDb.beginTx();
            try {
                tmpEdge.setProperty(RelationshipProperties.EDGE_WEIGHT, weight);
                tx.success();
            }
            finally {
                tx.finish();
            }
        }

        // commit node degree (needed for community label propagation)
        Transaction tx = graphDb.beginTx();
        try {
            node.setProperty(NodeProperties.NODE_DEGREE, nodeDegree);
            tx.success();
        }
        finally {
            tx.finish();
        }
    }

    public static void calculateNodeWeightAndCommunity(Node node, GraphDatabaseService graphDb) {
        Iterator<Relationship> nodeEdgesIter = node.getRelationships(Direction.OUTGOING).iterator();
        float nodeWeight = 0;

        while (nodeEdgesIter.hasNext()) {
            float edgeWeight = (Float)nodeEdgesIter.next().getProperty(RelationshipProperties.EDGE_WEIGHT);
            if(edgeWeight > nodeWeight)
                nodeWeight = edgeWeight;
        }

        // commit new properties
        Transaction tx = graphDb.beginTx();
        try {
            node.setProperty(NodeProperties.NODE_WEIGHT, nodeWeight);
            node.setProperty(NodeProperties.NODE_COMMUNITY, new Float((Integer)node.getProperty(NodeProperties.NODE_ID)));
            tx.success();
        } finally {
            tx.finish();
        }
    }

    public static List<GPNode> createGP(Node node, GraphDatabaseService graphDb) {
        List<Relationship> gpRel = new ArrayList<Relationship>();
        Iterator<Relationship> nodeEdgesIter = node.getRelationships(Direction.OUTGOING).iterator();

        while (nodeEdgesIter.hasNext()) {
            Relationship relationship = nodeEdgesIter.next();
            Node neighbour = relationship.getEndNode();
            if(((Float)node.getProperty(NodeProperties.NODE_WEIGHT)).equals((Float)neighbour.getProperty(NodeProperties.NODE_WEIGHT)))
                gpRel.add(relationship);
        }

        return RelationshipUtils.mapRelationshipsToGPNodes(gpRel);
    }

    public static void updateGraph(Map<Integer, GPNode> nodeToUpdate, Traverser traverser, GraphDatabaseService graphDb) {
        Transaction tx = graphDb.beginTx();
        try {
            for (Path path: traverser) {
                Node node = path.endNode();
                int nodeId = (Integer)node.getProperty(NodeProperties.NODE_ID);
                GPNode gpNode = nodeToUpdate.get(nodeId);

                // update
                if(gpNode != null) {
                    node.setProperty(NodeProperties.NODE_COMMUNITY, gpNode.getCommunity());
                    node.setProperty(NodeProperties.OLD_NODE_COMMUNITY, gpNode.getCommunity());
                } else {
                    node.setProperty(NodeProperties.OLD_NODE_COMMUNITY, node.getProperty(NodeProperties.NODE_COMMUNITY));
                }

            }
            tx.success();
        } finally {
            tx.finish();
        }
    }

    public static boolean calculateCommunity(Node node, GraphDatabaseService graphDb, int edgeCount) {
        boolean isStable = false;
        Iterator<Relationship> edgeIterator = node.getRelationships(Direction.OUTGOING).iterator();
        float[] labels = new float[(Integer)node.getProperty(NodeProperties.NODE_DEGREE)];
        int i = 0;

        while (edgeIterator.hasNext()) {
            Node neighbour = edgeIterator.next().getEndNode();
            labels[i] = UndirectedNodeUtils.calculateOldCommunity(neighbour, (Integer) node.getProperty(NodeProperties.NODE_ID), edgeCount);
            i++;
        }

        // calculate max occurrence of a label
        float maxLabel = UndirectedNodeUtils.labelMax(labels);
        if(maxLabel != (Float)node.getProperty(NodeProperties.OLD_NODE_COMMUNITY)) {
            Transaction tx = graphDb.beginTx();
            try {
                node.setProperty(NodeProperties.NODE_COMMUNITY, maxLabel);
                tx.success();
            } finally {
                tx.finish();
            }
            return false;
        }
        else
            return true;
    }

    private static float calculateOldCommunity(Node node, int srcNodeId, int edgeCount) {
        float nodeCommunity = (Float)node.getProperty(NodeProperties.OLD_NODE_COMMUNITY);
        float edgeWeight = 0;
        Iterator<Relationship> edgeIterator = node.getRelationships(Direction.OUTGOING).iterator();
        while (edgeIterator.hasNext()) {
            Relationship edge = edgeIterator.next();
            if((Integer)edge.getEndNode().getProperty(NodeProperties.NODE_ID) == srcNodeId) {
                edgeWeight = (Float)edge.getProperty(RelationshipProperties.EDGE_WEIGHT);
                break;
            }
        }

        int dc = UndirectedNodeUtils.communityTotalDegree.get(node.getProperty(NodeProperties.OLD_NODE_COMMUNITY));
        float labelWeight = 1 - ((float)dc / (float)(2 * edgeCount));

        return (nodeCommunity * edgeWeight * labelWeight);
    }

    /*
     Exchange community AND community' AND update community total degree
      */
    public static void updateCommunity(Traverser traverser, GraphDatabaseService graphDb) {
        Transaction tx = graphDb.beginTx();
        try {
            UndirectedNodeUtils.communityTotalDegree = new HashMap<Float, Integer>(); //clear old communities

            for (Path path: traverser) {
                Node node = path.endNode();
                node.setProperty(NodeProperties.OLD_NODE_COMMUNITY, node.getProperty(NodeProperties.NODE_COMMUNITY));

                //update community total degree
                int degree = (Integer)node.getProperty(NodeProperties.NODE_DEGREE);
                float community = (Float)node.getProperty(NodeProperties.NODE_COMMUNITY);
                Integer communityDegree = UndirectedNodeUtils.communityTotalDegree.get(community);
                if(communityDegree == null) {
                    communityDegree = new Integer(degree);
                    UndirectedNodeUtils.communityTotalDegree.put(community, communityDegree);
                } else {
                    UndirectedNodeUtils.communityTotalDegree.put(community, (degree+communityDegree));
                }
            }
            tx.success();
        } finally {
            tx.finish();
        }
    }

    public static void buildCommunityDegree(Traverser traverser) {
        for (Path path: traverser) {
            Node node = path.endNode();
            float community = (Float)node.getProperty(NodeProperties.OLD_NODE_COMMUNITY);
            int degree = (Integer)node.getProperty(NodeProperties.NODE_DEGREE);

            Integer communityDegree = UndirectedNodeUtils.communityTotalDegree.get(community);
            if(communityDegree == null) {
                communityDegree = new Integer(degree);
                UndirectedNodeUtils.communityTotalDegree.put(community, communityDegree);
            } else {
                UndirectedNodeUtils.communityTotalDegree.put(community, (degree+communityDegree));
            }
        }
    }

    private static float labelMax(float[] labels){
        if(labels.length == 0)
            return 0;

        float max = labels[0];
        for(int i = 0; i < labels.length; i++) {
            if(labels[i] > max)
                max = labels[i];
        }

        return max;
    }
    //todo old method replaced by labelMax
    private static float labelMaxOccurrence(float[] labels){
        if(labels.length == 0)
            return 0;

        Map<Float, Integer> modeMap = new HashMap<Float, Integer>();
        float maxEl = labels[0];
        int maxCount = 1;

        for(int i = 0; i < labels.length; i++) {
            float el = labels[i];
            if(modeMap.get(el) == null)
                modeMap.put(el, new Integer(1));
            else
                modeMap.put(el, modeMap.get(el)+1);
            if(modeMap.get(el) > maxCount) {
                maxEl = el;
                maxCount = modeMap.get(el);
            }
        }

        return  maxEl;
    }
}
