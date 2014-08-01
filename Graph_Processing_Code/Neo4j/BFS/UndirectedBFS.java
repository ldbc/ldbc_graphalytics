package org.test.neo4j.core;

import org.apache.log4j.Logger;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;
import org.test.neo4j.data.utils.NodeProperties;
import org.test.neo4j.data.utils.RelTypes;
import org.test.neo4j.data.utils.RelationshipProperties;
import org.test.neo4j.data.utils.UndirectedNodeUtils;
import org.test.neo4j.utils.Neo4Job;

import java.io.*;
import java.util.Iterator;

public class UndirectedBFS {
    private GraphDatabaseService graphDb;
    static Logger log = Logger.getLogger(UndirectedBFS.class.getName());

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
        log.info("Undirected BFS Texe = "+elapsedTimeSeconds+" ms");
        job.getJobExecutionStats().setTime(elapsedTimeSeconds);

        return 0;
    }
}
