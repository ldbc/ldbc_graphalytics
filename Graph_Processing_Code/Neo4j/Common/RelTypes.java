package org.test.neo4j.data.utils;

import org.neo4j.graphdb.RelationshipType;

public class RelTypes {
    public static enum NodeRelation implements RelationshipType {
        KNOWS, INIT_NODE
    }
}
