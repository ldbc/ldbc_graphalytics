/**
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import static nl.tudelft.graphalytics.neo4j.Neo4jConfiguration.EDGE;
import static nl.tudelft.graphalytics.neo4j.Neo4jConfiguration.ID_PROPERTY;
import static nl.tudelft.graphalytics.neo4j.Neo4jConfiguration.VertexLabelEnum.Vertex;

/**
 * Utility class for importing datasets into a new Neo4j database.
 *
 * @author Tim Hegeman
 */
public class Neo4jDatabaseImporter implements AutoCloseable {

    private final GraphDatabaseService database;
    private Transaction openTransaction;
    private int operationsInOpenTransaction;
    private final int maxOperationsPerTransactions;

    /**
     * @param database                     the underlying Neo4j database to insert nodes and relationships into
     * @param maxOperationsPerTransactions the maximum number of operations in a transaction before committing
     */
    public Neo4jDatabaseImporter(GraphDatabaseService database, int maxOperationsPerTransactions) {
        this.database = database;
        this.openTransaction = null;
        this.operationsInOpenTransaction = 0;
        this.maxOperationsPerTransactions = maxOperationsPerTransactions;
    }

    /**
     * Retrieves a Neo4j node with the given vertexId, or creates and returns a new node if it did not exist.
     *
     * @param vertexId the id of the vertex to find or create a Neo4j node for
     * @return the matching Neo4j node
     */
    public Node createOrGetNode(long vertexId) {
        ensureInTransaction();

        return createOrGetNodeInTransaction(vertexId);
    }

    /**
     * Creates a new (directed) edge between a pair of nodes identified by vertex ids. Both the source and destination
     * nodes are created if they do not exist.
     *
     * @param sourceVertexId      the id of the source vertex of the edge
     * @param destinationVertexId the id of the destination vertex of the edge
     */
    public void createEdge(long sourceVertexId, long destinationVertexId) {
        ensureInTransaction();

        Node source = createOrGetNodeInTransaction(sourceVertexId);
        Node destination = createOrGetNodeInTransaction(destinationVertexId);
        source.createRelationshipTo(destination, EDGE);
    }

    /**
     * Retrieves a Neo4j node with the given vertexId, or creates and returns a new node if it did not exist. Assumes
     * a transactions is active.
     *
     * @param vertexId the id of the vertex to find or create a Neo4j node for
     * @return the matching Neo4j node
     */
    private Node createOrGetNodeInTransaction(long vertexId) {
        Node node = findNode(vertexId);
        if (node == null) {
            node = database.createNode(Vertex);
            node.setProperty(ID_PROPERTY, vertexId);
        }
        return node;
    }

    /**
     * @param vertexId the id of the vertex to find a Neo4j node for
     * @return the matching Neo4j node if it exists, null otherwise
     */
    private Node findNode(long vertexId) {
        return database.findNode(Vertex, ID_PROPERTY, vertexId);
    }

    /**
     * Ensures that a transaction is active which has not yet reached its maximum number of operations. If no
     * transaction is active, it begins a new transaction. If a transaction is active and the maximum number of
     * operations has been reached, the active transaction is finalized and a new transaction is started. Finally, the
     * number of operations in the (new) active transaction is incremented.
     */
    private void ensureInTransaction() {
        if (openTransaction == null) {
            openTransaction = database.beginTx();
        } else if (operationsInOpenTransaction >= maxOperationsPerTransactions) {
            finalizeTransaction();
            openTransaction = database.beginTx();
            operationsInOpenTransaction = 0;
        }

        operationsInOpenTransaction++;
    }

    /**
     * Finalizes the active Neo4j transaction.
     */
    private void finalizeTransaction() {
        openTransaction.success();
        openTransaction.close();
    }

    @Override
    public void close() throws Exception {
        if (openTransaction != null) {
            finalizeTransaction();
            openTransaction = null;
        }
    }
}
