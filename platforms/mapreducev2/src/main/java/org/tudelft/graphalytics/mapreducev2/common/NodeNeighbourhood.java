package org.tudelft.graphalytics.mapreducev2.common;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

public class NodeNeighbourhood implements WritableComparable<NodeNeighbourhood>{
    private Node centralNode;
    private Vector<Node> nodeNeighbourhood;
    private final char ignoreChar = '#';

    public NodeNeighbourhood() {}
    public NodeNeighbourhood(Node node, Vector<Node> neighbourhood) {
        this.centralNode = node;
        this.nodeNeighbourhood = neighbourhood;
    }
    public NodeNeighbourhood(NodeNeighbourhood nodeNeighbourhood) {
        this.setCentralNode(nodeNeighbourhood.getCentralNode());
        this.setNodeNeighbourhood(nodeNeighbourhood.getNodeNeighbourhood());
    }

    public Node getCentralNode() { return centralNode; }
    public void setCentralNode(Node centralNode) { this.centralNode = centralNode; }

    public Vector<Node> getNodeNeighbourhood() { return nodeNeighbourhood; }
    public void setNodeNeighbourhood(Vector<Node> nodeNeighbourhood) { this.nodeNeighbourhood = nodeNeighbourhood; }

    public int compareTo(NodeNeighbourhood o) {
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
        Node tmpCentralNode = new Node();
        tmpCentralNode.readFields(nodesData[0]);
        this.setCentralNode(tmpCentralNode);

        // neighbours
        Vector<Node> tmpNodeNeighbourhood = new Vector<Node>();
        for(int i=1; i<nodesData.length; i++) {
            // build neighbour node
            Node node = new Node();
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

    private int compareNeighbourhood(Vector<Node> o) {
        int result = -1;

        if(this.getNodeNeighbourhood().size() < o.size())
            return -1;
        else if (this.getNodeNeighbourhood().size() > o.size())
            return 1;
        else {
            Iterator<Node> thisIter = this.getNodeNeighbourhood().iterator();
            Iterator<Node> oIter = o.iterator();
            while (thisIter.hasNext()) {
                Node thisNode = thisIter.next();
                Node oNode = oIter.next();
                if(thisNode.compareTo(oNode) != 0)
                    return thisNode.compareTo(oNode);
            }

        }

        return 0;
    }

    public String toFormattedString() {
        String result = this.getCentralNode().toString();

        Iterator<Node> neighbourhoodNodes = this.getNodeNeighbourhood().iterator();
        while(neighbourhoodNodes.hasNext()){
            result += "\n \t";
            Node tmpNode = neighbourhoodNodes.next();
            result += tmpNode.toString();
        }

        return result+"\n";
    }

    // nodeId \t nId,nId |nodeId@nId,nId|..
    public String toString() {
        boolean isFirst = true;
        String result = (this.getCentralNode().toText()).toString()+"|";
        Iterator<Node> neighbours = this.getNodeNeighbourhood().iterator();
        while(neighbours.hasNext()) {
            Node tmp = neighbours.next();
            if(isFirst) {
                boolean isFirstN = true;
                result += tmp.getId()+"@";
                for(Edge edge : tmp.getEdges()) {
                    if(isFirstN) {
                        result += edge.getDest();
                        isFirstN = false;
                    } else
                        result += ","+edge.getDest();
                }

                isFirst = false;
            } else {
                boolean isFirstN = true;
                result += "|"+tmp.getId()+"@";
                for(Edge edge : tmp.getEdges()) {
                    if(isFirstN) {
                        result += edge.getDest();
                        isFirstN = false;
                    } else
                        result += ","+edge.getDest();
                }
            }
        }

        return result;
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
