package org.test.neo4j.core;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;
import org.test.neo4j.data.utils.UndirectedNodeUtils;
import org.test.neo4j.data.utils.RelTypes;
import org.test.neo4j.utils.Neo4Job;

import java.io.IOException;
import java.util.Iterator;

public class UndirectedStats {
    private GraphDatabaseService graphDb;
    static Logger log = Logger.getLogger(UndirectedStats.class.getName());

    private Node getInitNode() {
        return graphDb.getReferenceNode().getSingleRelationship(
                RelTypes.NodeRelation.INIT_NODE, Direction.OUTGOING ).getEndNode();
    }

    private  Traverser getGraphTraverser(final Node initNode) throws IOException {
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.NodeRelation.KNOWS, Direction.BOTH)
                .evaluator(Evaluators.all());
        return td.traverse(initNode);
    }

    public int run(GraphDatabaseService graphDb, Neo4Job job) throws IOException {
        this.graphDb = graphDb;
        Traverser traverser = this.getGraphTraverser(this.getInitNode());

        log.info("Stats:");
        long t0 = System.currentTimeMillis();

        /*
            AVG Clustering coefficient and basic stats
         */
        int nodesNr = 0, edgesNr = 0;
        float ccSum = 0;

        for ( Path path: traverser) {
            Node centralNode = path.endNode();
            // basic stats
            nodesNr++;
            Iterator<Relationship> edgeIter = centralNode.getRelationships(Direction.OUTGOING).iterator();
            while (edgeIter.hasNext()) {
                edgeIter.next();
                edgesNr++;
            }

            // CC
            float cc = UndirectedNodeUtils.calculateNodeCC(centralNode);
            ccSum += cc;
        }

        log.info("NodeNr = "+nodesNr+" edgesNr = "+edgesNr/2);
        job.getJobExecutionStats().setEdgeSize(edgesNr/2);
        job.getJobExecutionStats().setNodeSize(nodesNr);
        log.info("Graph CC is "+(ccSum/nodesNr));

        long t1 = System.currentTimeMillis();
        double elapsedTimeSeconds = (t1 - t0)/1000.0;
        log.info("Undirected Stats Texe = "+elapsedTimeSeconds);
        job.getJobExecutionStats().setTime(elapsedTimeSeconds);

        return edgesNr/2;
    }
}
