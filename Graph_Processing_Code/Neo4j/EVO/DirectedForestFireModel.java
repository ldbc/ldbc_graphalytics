package org.test.neo4j.core;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;
import org.test.neo4j.data.utils.NodeProperties;
import org.test.neo4j.data.utils.RelTypes;
import org.test.neo4j.utils.GeometricalMeanUtil;
import org.test.neo4j.utils.Neo4Job;

import java.io.IOException;
import java.util.*;

/**
 Graph evolution algorithm Forest Fire Model

 Graphs over Time: Densification Laws, Shrinking Diameters and Possible Explanations
 by
 Jure Leskovec, Jon Kleinberg, Christos Faloutsos

 */
public class DirectedForestFireModel {
    protected GraphDatabaseService graphDb;
    protected List<Node> burnedVertices = new ArrayList<Node>(); // debug
    protected Set<Node> visited = new HashSet<Node>();
    protected GeometricalMeanUtil gmu = new GeometricalMeanUtil();
    protected Random rnd = new Random();
    static Logger log = Logger.getLogger(DirectedForestFireModel.class.getName());

    private List<Long> allVerticesIds = new ArrayList<Long>();

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

    /**
     *
     * @param
     */
    public int run(GraphDatabaseService graphDb, Neo4Job job) throws IOException {
        this.init(graphDb);

        long t0 = System.currentTimeMillis();
        this.collectVerticesIds();
        int maxID = job.getMaxID();

        for(int  i=0; i<job.getNewVertices(); i++) {
            gmu = new GeometricalMeanUtil();
            Node newVertex = this.runFFM(job.getpRation(), job.getrRation(), job.getHoops(), ++maxID);
            this.burnedVertices.add(newVertex);
            this.allVerticesIds.add(newVertex.getId());
            this.visited.clear();
        }

        long t1 = System.currentTimeMillis();
        double elapsedTimeSeconds = t1 - t0;
        job.getJobExecutionStats().setTime(elapsedTimeSeconds);

        log.info("Evolution Texe = "+elapsedTimeSeconds+" ms");

        return 0;
    }

    protected Node runFFM(float p, float r, int hoopsThreshold, int maxID) {
        long initAmbassadorID = this.getInitAmbassador(allVerticesIds); // returns neo4j internal ID
        Node initAmbassador = graphDb.getNodeById(initAmbassadorID);
        List<Node> currentStepAmbassadors = new ArrayList<Node>();
        currentStepAmbassadors.add(initAmbassador);
        List<Node> nextStepAmbassadors = new ArrayList<Node>();
        int hoops = 0;

        Transaction tx = graphDb.beginTx();
        Node newVertex = graphDb.createNode();
        newVertex.setProperty(NodeProperties.NODE_ID, String.valueOf(maxID));
        this.visited.add(newVertex);
        tx.success();
        tx.finish();

        do {
            nextStepAmbassadors.addAll(this.burn(newVertex, currentStepAmbassadors,p, r));
            currentStepAmbassadors.clear();
            currentStepAmbassadors.addAll(nextStepAmbassadors);
            nextStepAmbassadors.clear();

            hoops++;
        } while ( !(hoops >= hoopsThreshold) && currentStepAmbassadors.size()>0);

        return newVertex;
    }

    protected List<Node> burn(Node newVertex, List<Node> ambassadors, float p, float r) {
        List<Node> outNeighbours = new ArrayList<Node>();
        List<Node> inNeighbours = new ArrayList<Node>();
        List<Node> result = new ArrayList<Node>();
        Transaction tx;

        for(Node ambassador : ambassadors) {
            tx = graphDb.beginTx();

            /*
                OUT
             */
            Iterator<Relationship> neighbourIter = ambassador.getRelationships(Direction.OUTGOING).iterator();
            while (neighbourIter.hasNext()) {
                Node node = neighbourIter.next().getEndNode();
                if(!this.visited.contains(node))
                    outNeighbours.add(node);
            }

            double x = gmu.getGeoDev(1.0 - p);

            // burn as much as you can
            if(outNeighbours.size() <= x) {
                for(Node node : outNeighbours) {
                    newVertex.createRelationshipTo(node, RelTypes.NodeRelation.KNOWS);
                    node.createRelationshipTo(newVertex, RelTypes.NodeRelation.KNOWS);
                    result.add(node);
                    this.visited.add(node);
                }
            }
            // burn == X
            else {
                int maxIndex = outNeighbours.size();
                for(int i=(int)x; i>0; i--) {
                    int index = this.rnd.nextInt(maxIndex);
                    Node node = outNeighbours.get(index);

                    newVertex.createRelationshipTo(node, RelTypes.NodeRelation.KNOWS);
                    node.createRelationshipTo(newVertex, RelTypes.NodeRelation.KNOWS);
                    result.add(node);
                    this.visited.add(node);

                    outNeighbours.remove(index);
                    maxIndex = outNeighbours.size();
                }
            }

            /*
                IN
             */
            neighbourIter = ambassador.getRelationships(Direction.INCOMING).iterator();
            while (neighbourIter.hasNext()) {
                Node node = neighbourIter.next().getEndNode();
                if(!this.visited.contains(node))
                    outNeighbours.add(node);
            }

            double y = gmu.getGeoDev(1.0 - r);

            // burn as much as you can
            if(inNeighbours.size() <= y) {
                for(Node node : inNeighbours) {
                    newVertex.createRelationshipTo(node, RelTypes.NodeRelation.KNOWS);
                    node.createRelationshipTo(newVertex, RelTypes.NodeRelation.KNOWS);
                    result.add(node);
                    this.visited.add(node);
                }
            }
            // burn == Y
            else {
                int maxIndex = inNeighbours.size();
                for(int i=(int)y; i>0; i--) {
                    int index = this.rnd.nextInt(maxIndex);
                    Node node = inNeighbours.get(index);

                    newVertex.createRelationshipTo(node, RelTypes.NodeRelation.KNOWS);
                    node.createRelationshipTo(newVertex, RelTypes.NodeRelation.KNOWS);
                    result.add(node);
                    this.visited.add(node);

                    inNeighbours.remove(index);
                    maxIndex = inNeighbours.size();
                }
            }

            tx.success();
            tx.finish();
            outNeighbours.clear();
            inNeighbours.clear();
        }

        return result;
    }

    /*
        HELPERS
     */
    protected void init(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    protected void collectVerticesIds() throws IOException {
        Traverser traverser = this.getGraphTraverser(this.getInitNode());

        for ( Path path: traverser) {
            Node currentEndNode = path.endNode();
            long id = currentEndNode.getId(); // storing neo4J db id
            allVerticesIds.add(id);
        }
    }

    protected long getInitAmbassador(List<Long> allVerticesIds) {
        int index = this.rnd.nextInt(allVerticesIds.size());
        return allVerticesIds.get(index);
    }
}

