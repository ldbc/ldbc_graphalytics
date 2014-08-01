package org.test.neo4j.core;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;
import org.test.neo4j.data.utils.NodeProperties;
import org.test.neo4j.data.utils.RelTypes;
import org.test.neo4j.utils.Neo4Job;

import java.io.*;

public class DirectedBFS {
    private GraphDatabaseService graphDb;
    static Logger log = Logger.getLogger(DirectedBFS.class.getName());

    private Node getInitNode() {
        return graphDb.getReferenceNode().getSingleRelationship(
                RelTypes.NodeRelation.INIT_NODE, Direction.OUTGOING ).getEndNode();
    }

    private Traverser getGraphTraverser(final Node initNode) throws IOException {
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.NodeRelation.KNOWS, Direction.OUTGOING)
                .evaluator(Evaluators.all());
        return td.traverse(initNode);
    }

    public int run(GraphDatabaseService graphDb, Neo4Job job) throws IOException {
        this.graphDb = graphDb;
        Traverser traverser = this.getGraphTraverser(this.getInitNode());

        log.info("BFS: "+job.getJobInput());
        long t0 = System.currentTimeMillis();

        Node srcNode = this.getInitNode();
        for ( Path path: traverser) {
            int distance = path.length();
            Node currentEndNode = path.endNode();
        }

        long t1 = System.currentTimeMillis();        
        double elapsedTimeSeconds = t1 - t0;
        log.info("Directed BFS Texe = "+elapsedTimeSeconds+" ms");
        job.getJobExecutionStats().setTime(elapsedTimeSeconds);     

        return 0;
    }
}
