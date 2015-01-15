package nl.tudelft.graphalytics.mapreducev2.evo;

import org.apache.hadoop.io.LongWritable;

import java.util.*;

/**
 Utils for FFM graph evolution
 */

/**
 * @author Marcin Biczak
 */
public class ForestFireModelUtils {
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
    public static String verticesIDsMap2String(Map<LongWritable, List<LongWritable>> map) {
        String result = new String();
        Set<LongWritable> keys = map.keySet();
        boolean isHead = true;

        for(LongWritable newVertex : keys) {
            boolean isFirst = true;

            String vertex = new String(newVertex.get()+"@");

            for(LongWritable elem : map.get(newVertex)) {
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
    public static Map<LongWritable, List<LongWritable>> verticesIdsString2Map(String mapStr) {
        Map<LongWritable, List<LongWritable>> map = new HashMap<LongWritable, List<LongWritable>>();

        StringTokenizer vertexTokenizer = new StringTokenizer(mapStr, "|");
        while (vertexTokenizer.hasMoreElements()) {
            String vertex = vertexTokenizer.nextToken();
            String[] data = vertex.split("@");
            String[] edges = data[1].split(",");

            for(String edge : edges) {
                long edgeInt = Long.parseLong(edge);
                if(map.containsKey(new LongWritable(edgeInt))) {
                    List<LongWritable> newVertices = map.get(new LongWritable(edgeInt));
                    newVertices.add(new LongWritable(Long.parseLong(data[0])));
                    map.put(new LongWritable(edgeInt), newVertices);
                } else {
                    List<LongWritable> newVertices = new ArrayList<LongWritable>();
                    newVertices.add(new LongWritable(Long.parseLong(data[0])));
                    map.put(new LongWritable(edgeInt), newVertices);
                }
            }
        }

        return map;
    }
}
