package org.tudelft.graphalytics.mapreducev2.bfs;

/**
 * Configuration constants for breadth-first search on MapReduce version 2.
 *
 * @author Tim Hegeman
 */
public final class BreadthFirstSearchConfiguration {
    public static final String SOURCE_VERTEX_KEY = "BFS.source";

    public enum NODE_STATUS {
        NOT_VISITED,
        VISITED
    }
}
