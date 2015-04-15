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

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

/**
 * @author Marcin Biczak
 */
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
        StringBuilder result = new StringBuilder();
        boolean isFirst = true;
        result.append(this.getId()).append("\t");
        Iterator<Edge> iterator = this.getEdges().iterator();
        while(iterator.hasNext()) {
            Edge edge = iterator.next();
            if(isFirst) {
                result.append(edge.getDest());
                isFirst = false;
            } else
                result.append(",").append(edge.getDest());
        }

        return new Text(result.toString());
    }

    public Text toTextConnectedComponent() {
        StringBuilder result = new StringBuilder();
        boolean isFirst = true;
        result.append(this.getId()).append("\t").append(this.getId()).append("$").append("\t");
        Iterator<Edge> iterator = this.getEdges().iterator();
        while(iterator.hasNext()) {
            Edge edge = iterator.next();
            if(isFirst) {
                result.append(edge.getDest());
                isFirst = false;
            }
            else
                result.append(", ").append(edge.getDest());
        }

        return new Text(result.toString());
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
        String nodeID = null;

        if(nodeLine.charAt(0) != '#') {
            StringTokenizer tokenizer = new StringTokenizer(nodeLine, " \t\n\r\f,.:;?![]'");
            if(tokenizer.countTokens() > 0) {
                nodeID = tokenizer.nextToken();
            }
            else
                throw new IOException("Error while reading. File format not supported.");
        }

        if(nodeID != null && !nodeID.isEmpty())
            return nodeID;
        else
            throw  new IOException("Unable to read nodeID");
    }
}
