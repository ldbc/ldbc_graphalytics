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

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * CURRENT
 Proper implementation of Giraph/Hadoop algorithm
 */
public class ConnectedComponent {
    private GraphDatabaseService graphDb;
    static Logger log = Logger.getLogger(ConnectedComponent.class.getName());
    private int smallestComponentID = 0;

    private Node getInitNode() {
        return graphDb.getReferenceNode().getSingleRelationship(
                RelTypes.NodeRelation.INIT_NODE, Direction.OUTGOING ).getEndNode();
    }

    private Traverser getGraphTraverser(final Node initNode) throws IOException {
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.NodeRelation.KNOWS, Direction.BOTH)
                .evaluator(Evaluators.all());
        return td.traverse(initNode);
    }

    public int run(GraphDatabaseService graphDb, Neo4Job job) throws IOException {
        boolean isFinished = false;
        this.graphDb = graphDb;
        Traverser traverser = this.getGraphTraverser(this.getInitNode());
        this.initGraph(graphDb);

        log.info("Connected Component: "+job.getJobInput());
        long t0 = System.currentTimeMillis();

        Transaction tx = graphDb.beginTx();

        while (!isFinished) {
            isFinished = true;
            for(Path path: traverser) {
                Node current = path.endNode();
                Set<String> neighbours = this.getNeighboursComponentId(current, job);

                //search for lowest component ID
                String currentComponentID = (String)current.getProperty(NodeProperties.NODE_COMPONENT);
                int currentCompID = Integer.valueOf(currentComponentID);

                for(String neighbourCompId : neighbours) {
                    int compID = Integer.valueOf(neighbourCompId);
                    if(currentCompID > compID) {    // found smaller component ID, continue algorithm
                        currentCompID = compID;
                        current.setProperty(NodeProperties.NODE_COMPONENT, String.valueOf(currentCompID));
                        isFinished = false;
                    }
                }

            }

            tx.success();
            tx.finish();
            tx = graphDb.beginTx();
        }

        // close last iteration started transaction
        tx.success();
        tx.finish();

        long t1 = System.currentTimeMillis();
        double elapsedTimeSeconds = t1 - t0;
        log.info("ConnectedComponents Texe = "+elapsedTimeSeconds+" ms");
        job.getJobExecutionStats().setTime(elapsedTimeSeconds);

        return 0;
    }

    private Set<String> getNeighboursComponentId(Node node, Neo4Job job) {
        Set<String> uniqueNeighbours = new HashSet<String>();

        Iterator<Relationship> edges = node.getRelationships(Direction.OUTGOING).iterator(); // OUT edges
        while (edges.hasNext()) {
            uniqueNeighbours.add((String)edges.next().getEndNode().getProperty(NodeProperties.NODE_COMPONENT));
        }

        // only for directed graphs
        if(job.getType().equals("directed")) {
            edges = node.getRelationships(Direction.INCOMING, RelTypes.NodeRelation.KNOWS).iterator(); // IN edges, relationship restriction to avoid INIT_NODE
            while (edges.hasNext())
                uniqueNeighbours.add((String)edges.next().getStartNode().getProperty(NodeProperties.NODE_COMPONENT));
        }

        return uniqueNeighbours;
    }

    // set node id as component id
    private int initGraph(GraphDatabaseService graphDb) throws IOException{
        this.graphDb = graphDb;
        Traverser traverser = this.getGraphTraverser(this.getInitNode());
        Transaction tx = graphDb.beginTx();

        log.info("initializing each node with component ID = Node.ID");

        for(Path path: traverser) {
            Node current = path.endNode();
            String nodeID = (String)current.getProperty(NodeProperties.NODE_ID);
            current.setProperty(NodeProperties.NODE_COMPONENT, nodeID);
        }

        log.info("Nodes component ID initialized");

        tx.success();
        tx.finish();

        return 0;
    }
}
