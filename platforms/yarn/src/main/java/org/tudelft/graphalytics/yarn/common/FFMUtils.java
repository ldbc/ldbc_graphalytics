package org.tudelft.graphalytics.yarn.common;

import org.apache.hadoop.io.VIntWritable;

import java.util.*;

/**
 Utils for FFM graph evolution
 */
public class FFMUtils {
    // CONF
    public static final String MAX_ID = "MAX_ID";
    public static final String NEW_VERTICES_NR = "NEW_VERTICES_NR";
    public static final String P_RATIO = "P_RATIO";
    public static final String R_RATIO = "R_RATIO";
    public static final String ID_SHIFT = "ID_SHIFT";
    public static final String IS_INIT = "IS_INIT";
    public static final  String CURRENT_AMBASSADORS = "CURRENT_AMBASSADORS";

    // Counters
    public static final String NEW_VERTICES = "NEW_VERTICES";

    /*
       HELPERS
    */
    // Map<newVertex, [Ambassador]>
    // vertexID@edges,edges|vertexID@edges,edges
    public static String verticesIDsMap2String(Map<VIntWritable, List<VIntWritable>> map) {
        String result = new String();
        Set<VIntWritable> keys = map.keySet();
        boolean isHead = true;

        for(VIntWritable newVertex : keys) {
            boolean isFirst = true;

            String vertex = new String(newVertex.get()+"@");

            for(VIntWritable elem : map.get(newVertex)) {
                if(isFirst) {
                    vertex += elem;
                    isFirst = false;
                } else
                    vertex += ","+elem;
            }

            if(isHead) {
                result += vertex;
                isHead = false;
            } else
                result += "|"+vertex;
        }

        return result;
    }

    /**
     *  revert string -> Map<Ambassador, [NewVertex]>
     *
     * @param mapStr
     * @return
     */
    public static Map<VIntWritable, List<VIntWritable>> verticesIdsString2Map(String mapStr) {
        Map<VIntWritable, List<VIntWritable>> map = new HashMap<VIntWritable, List<VIntWritable>>();

        StringTokenizer vertexTokenizer = new StringTokenizer(mapStr, "|");
        while (vertexTokenizer.hasMoreElements()) {
            String vertex = vertexTokenizer.nextToken();
            String[] data = vertex.split("@");
            String[] edges = data[1].split(",");

            for(String edge : edges) {
                int edgeInt =Integer.parseInt(edge);
                if(map.containsKey(new VIntWritable(edgeInt))) {
                    List<VIntWritable> newVertices = map.get(new VIntWritable(edgeInt));
                    newVertices.add(new VIntWritable(Integer.parseInt(data[0])));
                    map.put(new VIntWritable(edgeInt), newVertices);
                } else {
                    List<VIntWritable> newVertices = new ArrayList<VIntWritable>();
                    newVertices.add(new VIntWritable(Integer.parseInt(data[0])));
                    map.put(new VIntWritable(edgeInt), newVertices);
                }
            }
        }

        return map;
    }
}
