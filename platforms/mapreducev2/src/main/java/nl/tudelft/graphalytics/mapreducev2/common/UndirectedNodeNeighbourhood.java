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
package nl.tudelft.graphalytics.mapreducev2.common;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author Marcin Biczak
 */
public class UndirectedNodeNeighbourhood implements WritableComparable<UndirectedNodeNeighbourhood>{
    private UndirectedNode centralNode;
    private Vector<UndirectedNode> nodeNeighbourhood;
    private final char ignoreChar = '#';

    public UndirectedNodeNeighbourhood() {}
    public UndirectedNodeNeighbourhood(UndirectedNode node, Vector<UndirectedNode> neighbourhood) {
        this.centralNode = node;
        this.nodeNeighbourhood = neighbourhood;
    }
    public UndirectedNodeNeighbourhood(UndirectedNodeNeighbourhood nodeNeighbourhood) {
        this.setCentralNode(nodeNeighbourhood.getCentralNode());
        this.setNodeNeighbourhood(nodeNeighbourhood.getNodeNeighbourhood());
    }

    public UndirectedNode getCentralNode() { return centralNode; }
    public void setCentralNode(UndirectedNode centralNode) { this.centralNode = centralNode; }

    public Vector<UndirectedNode> getNodeNeighbourhood() { return nodeNeighbourhood; }
    public void setNodeNeighbourhood(Vector<UndirectedNode> nodeNeighbourhood) { this.nodeNeighbourhood = nodeNeighbourhood; }

    public int compareTo(UndirectedNodeNeighbourhood o) {
        return ((this.getCentralNode().compareTo(o.getCentralNode())) != 0)
            ? (this.getCentralNode().compareTo(o.getCentralNode()))
            : this.compareNeighbourhood(o.getNodeNeighbourhood());
    }

    public void write(DataOutput dataOutput) throws IOException {
        String nodeNeighAsString = this.toString();
        dataOutput.writeBytes(nodeNeighAsString);
    }

    public void readFields(DataInput input) throws IOException {
        String nodeNeighbourhoodLine = input.readLine();
        this.readFields(nodeNeighbourhoodLine);
    }

    /*
        centralNodeId \t [nId,..] |neighbourID@[dst]|neighbourID@[dst]
        2	1,3,4|1@2,3|3@2|4@2
     */
    public void readFields(String nodeNeighbourhoodLine) throws IOException {
        StringTokenizer nodesTokenizer = new StringTokenizer(nodeNeighbourhoodLine,"|");
        String[] nodesData = new String[nodesTokenizer.countTokens()];
        int x=0;
        while (nodesTokenizer.hasMoreTokens()) {
            nodesData[x] = nodesTokenizer.nextToken();
            x++;
        }

        // central node
        UndirectedNode tmpCentralNode = new UndirectedNode();
        tmpCentralNode.readFields(nodesData[0]);
        this.setCentralNode(tmpCentralNode);

        // neighbours
        Vector<UndirectedNode> tmpNodeNeighbourhood = new Vector<UndirectedNode>();
        for(int i=1; i<nodesData.length; i++) {
            // build neighbour node
            UndirectedNode node = new UndirectedNode();
            StringTokenizer neighbourTokenizer = new StringTokenizer(nodesData[i], "@");
            if(neighbourTokenizer.countTokens() == 2) {
                node.setId(neighbourTokenizer.nextToken());
                StringTokenizer edgeTokenizer = new StringTokenizer(neighbourTokenizer.nextToken(),",");
                Vector<Edge> nodeEdges = new Vector<Edge>();
                while (edgeTokenizer.hasMoreTokens())
                    nodeEdges.add(new Edge(node.getId(), edgeTokenizer.nextToken()));

                node.setEdges(nodeEdges);
            } else
                throw new IOException("Neighbouring node in NodeNeighbourhood has != 2 tokens, it has "+neighbourTokenizer.countTokens());

            tmpNodeNeighbourhood.add(node);
        }

        this.setNodeNeighbourhood(tmpNodeNeighbourhood);
    }

    private int compareNeighbourhood(Vector<UndirectedNode> o) {
        int result = -1;

        if(this.getNodeNeighbourhood().size() < o.size())
            return -1;
        else if (this.getNodeNeighbourhood().size() > o.size())
            return 1;
        else {
            Iterator<UndirectedNode> thisIter = this.getNodeNeighbourhood().iterator();
            Iterator<UndirectedNode> oIter = o.iterator();
            while (thisIter.hasNext()) {
                UndirectedNode thisNode = thisIter.next();
                UndirectedNode oNode = oIter.next();
                if(thisNode.compareTo(oNode) != 0)
                    return thisNode.compareTo(oNode);
            }

        }

        return 0;
    }

    public String toFormattedString() {
        StringBuilder result = new StringBuilder(this.getCentralNode().toString());

        Iterator<UndirectedNode> neighbourhoodNodes = this.getNodeNeighbourhood().iterator();
        while(neighbourhoodNodes.hasNext()){
            result.append("\n \t");
            UndirectedNode tmpNode = neighbourhoodNodes.next();
            result.append(tmpNode.toString());
        }

        return result.append("\n").toString();
    }

    // nodeId \t nId,nId |nodeId@nId,nId|..
    public String toString() {
        boolean isFirst = true;
        StringBuilder result = new StringBuilder(this.getCentralNode().toText().toString()).append("|");
        Iterator<UndirectedNode> neighbours = this.getNodeNeighbourhood().iterator();
        while(neighbours.hasNext()) {
            UndirectedNode tmp = neighbours.next();
            if(isFirst) {
                boolean isFirstN = true;
                result.append(tmp.getId()).append("@");
                for(Edge edge : tmp.getEdges()) {
                    if(isFirstN) {
                        result.append(edge.getDest());
                        isFirstN = false;
                    } else
                        result.append(",").append(edge.getDest());
                }

                isFirst = false;
            } else {
                boolean isFirstN = true;
                result.append("|").append(tmp.getId()).append("@");
                for(Edge edge : tmp.getEdges()) {
                    if(isFirstN) {
                        result.append(edge.getDest());
                        isFirstN = false;
                    } else
                        result.append(",").append(edge.getDest());
                }
            }
        }

        return result.toString();
    }

//    public Vector<Triad> getTriads() {
//        Vector<Triad> result = new Vector<Triad>();
//
//        Vector<Edge> neighboursEdges = new Vector<Edge>();
//
//        for (Node node : this.getNodeNeighbourhood()) {
//            neighboursEdges.addAll(node.getEdges());
//        }
//
//        for(int i=0; i<neighboursEdges.size(); i++) {
//            Edge tmpEdge = neighboursEdges.get(i);
//            for(int j=0; j<neighboursEdges.size(); j++) {
//                if(i == j) continue;
//                Edge tmpEdge2 = neighboursEdges.get(j);
//                if(tmpEdge.compareTo(tmpEdge2.swapEdge()) == 0) {
//                    result.add(new Triad(centralNode.getId(), tmpEdge.getSrc(), tmpEdge.getDest()));
//                    neighboursEdges.remove(j); //speedup -> innerloop
//                    neighboursEdges.remove(i); //speedup -> innerloop
//                    i--; //balance removal
//                    break;
//                }
//            }
//        }
//
//        return result;
//    }
}
