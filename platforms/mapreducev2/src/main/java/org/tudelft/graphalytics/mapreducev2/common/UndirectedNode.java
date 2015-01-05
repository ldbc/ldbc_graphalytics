package org.tudelft.graphalytics.mapreducev2.common;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

public class UndirectedNode implements WritableComparable<UndirectedNode> {
    private String id;
    private Vector<Edge> edges;
    private final char ignoreChar = '#';

    public UndirectedNode() {}

    public UndirectedNode(String id, Vector<Edge> edges) {
        this.id = id;
        this.edges = edges;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Vector<Edge> getEdges() { return edges; }
    public void setEdges(Vector<Edge> edges) { this.edges = edges; }

    public int compareTo(UndirectedNode o) {
        System.out.println("NODE_STD_COMPARATOR");
        return (this.getId().compareTo(o.getId()) != 0)
            ? this.getId().compareTo(o.getId())
            : this.compareEdges(o.getEdges());
    }

    public void write(DataOutput dataOutput) throws IOException {
        String nodeAsString = this.toString();
        dataOutput.writeBytes(nodeAsString);
    }

    public void readFields(DataInput input) throws IOException {
        String nodeLine = input.readLine();
        this.readFields(nodeLine);
    }

    public void readFields(String nodeLine) throws IOException {
        if(nodeLine.charAt(0) != this.ignoreChar) {
            StringTokenizer tokenizer = new StringTokenizer(nodeLine, " \t\n\r\f,.:;?![]'");
            if(tokenizer.countTokens() > 0) {
                this.setId(tokenizer.nextToken());
                Vector<Edge> tmpEdgeList = new Vector<Edge>();
                while(tokenizer.hasMoreElements()) {
                    tmpEdgeList.add(new Edge(this.id, tokenizer.nextToken()));
                }
                this.setEdges(tmpEdgeList);
            }
            else
                throw new IOException("Error while reading. File format not supported.");
        }
    }

    public int compareEdges(Vector<Edge> o) {
        if(this.getEdges().size() < o.size())
            return -1;
        else if (this.getEdges().size() > o.size())
            return 1;
        else {
            Iterator thisIter = this.getEdges().iterator();
            Iterator oIter = o.iterator();
            while (thisIter.hasNext()) {
                Edge thisEdge = (Edge) thisIter.next();
                Edge oEdge = (Edge) oIter.next();
                if(thisEdge.compareTo(oEdge) != 0)
                    return thisEdge.compareTo(oEdge);
            }

        }

        return 0;
    }

    public String toString(){
        return this.toText()+" \n";
    }

    public Text toText() {
        String result = new String();
        boolean isFirst = true;
        result += this.getId()+"\t";
        Iterator<Edge> iterator = this.getEdges().iterator();
        while(iterator.hasNext()) {
            Edge edge = iterator.next();
            if(isFirst) {
                result += edge.getDest();
                isFirst = false;
            } else
                result +=","+edge.getDest();
        }

        return new Text(result);
    }

    public Text toTextConnectedComponent() {
        String result = new String();
        boolean isFirst = true;
        result += this.getId()+"\t"+this.getId()+"$"+"\t";
        Iterator<Edge> iterator = this.getEdges().iterator();
        while(iterator.hasNext()) {
            Edge edge = iterator.next();
            if(isFirst) {
                result += edge.getDest();
                isFirst = false;
            }
            else
                result += ", "+edge.getDest();
        }

        return new Text(result);
    }

    public UndirectedNode copy() {
        UndirectedNode newObj = new UndirectedNode();
        newObj.setId(this.getId());
        newObj.setEdges(this.getEdges());
        return newObj;
    }

    /*
        Read only ID of the Node
     */
    public static String readNodeId(String nodeLine) throws IOException{
        String nodeID = new String();

        if(nodeLine.charAt(0) != '#') {
            StringTokenizer tokenizer = new StringTokenizer(nodeLine, " \t\n\r\f,.:;?![]'");
            if(tokenizer.countTokens() > 0) {
                nodeID = tokenizer.nextToken();
            }
            else
                throw new IOException("Error while reading. File format not supported.");
        }

        if(! nodeID.isEmpty())
            return nodeID;
        else
            throw  new IOException("Unable to read nodeID");
    }
}
