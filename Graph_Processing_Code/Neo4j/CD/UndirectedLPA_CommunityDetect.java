package org.test.neo4j.core;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;
import org.test.neo4j.data.utils.NodeProperties;
import org.test.neo4j.data.utils.RelTypes;
import org.test.neo4j.utils.Neo4Job;

import java.io.*;
import java.util.*;

/**
 Towards Real-Time Community Detection in Large Networks
 by
 Ian X.Y. Leung,Pan Hui,Pietro Li,and Jon Crowcroft

 GOTCHA
 todo Neo4j when invoking node.setProperty() -> commits changes immediately, no transactions needed, THUS the step*Map force the iteration synchronization
 */
public class UndirectedLPA_CommunityDetect {
    static Logger LOG = Logger.getLogger(UndirectedForestFireModel.class.getName());
    protected GraphDatabaseService graphDb;
    protected  float mParam = 0;
    protected  float delta = 0;
    protected Random rnd = new Random();

    // used to FORCE iteration synchronization
    protected Map<String, String> stepLabelsMapping = new HashMap<String, String>(); // k - nodeID | v - label
    protected Map<String, Float> stepScoreMapping = new HashMap<String, Float>(); // k - nodeID | v - score

    protected void applyLabels() throws IOException {
        Traverser traverser = this.getGraphTraverser(this.getInitNode());
        Transaction tx = graphDb.beginTx();

        for ( Path path: traverser) {
            Node node = path.endNode();
            node.setProperty(NodeProperties.NODE_COMPONENT, this.stepLabelsMapping.get((String)node.getProperty(NodeProperties.NODE_ID)));
            node.setProperty(NodeProperties.NODE_COMPONENT_SCORE, this.stepScoreMapping.get((String)node.getProperty(NodeProperties.NODE_ID)));
        }

        tx.success();
        tx.finish();
    }

    protected void init(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    protected Node getInitNode() {
        return graphDb.getReferenceNode().getSingleRelationship(
                RelTypes.NodeRelation.INIT_NODE, Direction.OUTGOING ).getEndNode();
    }

    protected Traverser getGraphTraverser(final Node initNode) throws IOException {
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.NodeRelation.KNOWS, Direction.BOTH)
                .evaluator(Evaluators.all());
        return td.traverse(initNode);
    }

    public int run(GraphDatabaseService graphDb, Neo4Job job) throws IOException {
        this.init(graphDb);

        long t0 = System.currentTimeMillis();

        Transaction tx = graphDb.beginTx();
        this.delta = job.getDelta();
        this.mParam = job.getMParam();

        this.initAlg();

        for(int  i=0; i<job.getIterations(); i++) {
            Traverser traverser = this.getGraphTraverser(this.getInitNode());

            for ( Path path: traverser) {
                Node node = path.endNode();
                this.updateLabel(node);
            }

            tx.success();
            tx.finish();
            tx = this.graphDb.beginTx();

            this.applyLabels(); // FORCED Synchronize data at step T
        }

        // close last iteration started transaction
        tx.success();
        tx.finish();

        long t1 = System.currentTimeMillis();

        double elapsedTimeSeconds = t1 - t0;
        job.getJobExecutionStats().setTime(elapsedTimeSeconds);

        LOG.info("Comm_Detect Texe = "+elapsedTimeSeconds+" ms");

        return 0;
    }


    protected void initAlg() throws IOException{
        Traverser traverser = this.getGraphTraverser(this.getInitNode());
        Transaction tx = graphDb.beginTx();

        for ( Path path: traverser) {
            Node node = path.endNode();
            node.setProperty(NodeProperties.NODE_COMPONENT, node.getProperty(NodeProperties.NODE_ID));
            node.setProperty(NodeProperties.NODE_COMPONENT_SCORE, 1.0f);
        }

        tx.success();
        tx.finish();
    }

    protected void updateLabel(Node node) {
        float maxLabelScore = -100; // nasty workaround for label with score smaller than 0
        Map<String, Float> neighboursLabels = new HashMap<String, Float>(); // key - label, value - output of EQ 2
        Map<String, Float> labelsMaxScore = new HashMap<String, Float>();   // helper struct for updating new label score
        String newLabel = new String();
        String oldLabel = (String)node.getProperty(NodeProperties.NODE_COMPONENT);

        // gather labels
        // OUT
        Iterator<Relationship> edgesIter = node.getRelationships(Direction.OUTGOING).iterator();
        while (edgesIter.hasNext()) {
            Node neighbour = edgesIter.next().getEndNode();

            float eq2 = this.eq2(neighbour);
            String neighbourLabel = (String)neighbour.getProperty(NodeProperties.NODE_COMPONENT);
            float neighbourLabelScore = (Float)neighbour.getProperty(NodeProperties.NODE_COMPONENT_SCORE);

            if(neighboursLabels.containsKey(neighbourLabel)) {
                float labelAggScore = neighboursLabels.get(neighbourLabel);
                labelAggScore = labelAggScore + eq2;    // label aggregated score
                neighboursLabels.put(neighbourLabel, labelAggScore);

                // check if max score for this label
                if(labelsMaxScore.get(neighbourLabel) < neighbourLabelScore)
                    labelsMaxScore.put(neighbourLabel, neighbourLabelScore);
            } else {
                neighboursLabels.put(neighbourLabel, eq2);
                labelsMaxScore.put(neighbourLabel, neighbourLabelScore);
            }
        }

        // chose MAX score label OR random tie break
        Iterator<String> labelIter = neighboursLabels.keySet().iterator();
        List<String> potentialLabels = new ArrayList<String>();
        while (labelIter.hasNext()) {
            String tmpLabel = labelIter.next();
            float labelAggScore = neighboursLabels.get(tmpLabel);

            if(labelAggScore > maxLabelScore) {
                maxLabelScore = labelAggScore;
                newLabel = new String(tmpLabel.toString());
                potentialLabels.clear();
                potentialLabels.add(tmpLabel);
            }
            else if (labelAggScore == maxLabelScore)
                potentialLabels.add(tmpLabel);
        }

        // random tie break
        if(potentialLabels.size() > 1) {
            int labelIndex = rnd.nextInt(potentialLabels.size());
            newLabel = new String(potentialLabels.get(labelIndex));
        }

        // set delta param value
        float currentDelta;
        if(newLabel.equals(oldLabel))
            currentDelta = 0;
        else
            currentDelta = this.delta;

        // update label && new label score
        float newLabelScore = this.updateLabelScore(labelsMaxScore.get(newLabel), currentDelta);
        this.stepLabelsMapping.put((String)node.getProperty(NodeProperties.NODE_ID), newLabel);
        this.stepScoreMapping.put((String)node.getProperty(NodeProperties.NODE_ID), newLabelScore);
    }

    protected float eq2(Node node) {
        float labelScore = (Float)node.getProperty(NodeProperties.NODE_COMPONENT_SCORE);
        int function = 0;
        Iterator<Relationship> outDgreeIter = node.getRelationships(Direction.OUTGOING).iterator();
        while (outDgreeIter.hasNext()) {
            outDgreeIter.next();
            function++;
        }

        return (labelScore * (float)Math.pow((double)function, (double) this.mParam));
    }

    private float updateLabelScore(float score, float delta) {
        return score - delta;
    }
}
