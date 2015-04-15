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
public class DirectedNode implements WritableComparable<DirectedNode> {
    private String id;
    private Vector<Edge> inEdges;
    private Vector<Edge> outEdges;
    private final char ignoreChar = '#';

    public DirectedNode() {}

    public DirectedNode(String id, Vector<Edge> inEdges, Vector<Edge> outEdges) {
        this.id = id;
        this.inEdges = inEdges;
        this.outEdges = outEdges;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Vector<Edge> getInEdges() { return inEdges; }
    public void setInEdges(Vector<Edge> inEdges) { this.inEdges = inEdges; }

    public Vector<Edge> getOutEdges() { return outEdges; }
    public void setOutEdges(Vector<Edge> outEdges) { this.outEdges = outEdges; }

    public int compareTo(DirectedNode o) {
        return (this.getId().compareTo(o.getId()) != 0)
            ? this.getId().compareTo(o.getId())
            : this.compareAllEdges(o.getInEdges(), o.getOutEdges());
    }

    public void write(DataOutput dataOutput) throws IOException {
        String nodeAsString = this.toString();
        dataOutput.writeBytes(nodeAsString);
    }

    public void readFields(DataInput input) throws IOException {
        String nodeLine = input.readLine();
        this.readFields(nodeLine);
    }

    // id \t #[id,id] \t @[id,id]
    public void readFields(String nodeLine) throws IOException {
        if(nodeLine.charAt(0) != this.ignoreChar) {
            StringTokenizer tokenizer = new StringTokenizer(nodeLine, "#@");
            if(tokenizer.countTokens() > 1) {
                // id
                this.setId(new StringTokenizer(tokenizer.nextToken(), "\t").nextToken()); // "id\t"

                // IN
                StringTokenizer edgeTokenizer = new StringTokenizer(tokenizer.nextToken(), " \t\n\r\f,.:;?![]'");
                Vector<Edge> tmpEdgeList = new Vector<Edge>();
                while(edgeTokenizer.hasMoreElements()) {
                    tmpEdgeList.add(new Edge(edgeTokenizer.nextToken(), this.id));
                }
                this.setInEdges(tmpEdgeList);

                // OUT
                edgeTokenizer = new StringTokenizer(tokenizer.nextToken(), " \t\n\r\f,.:;?![]'");
                tmpEdgeList = new Vector<Edge>();
                while(edgeTokenizer.hasMoreElements()) {
                    tmpEdgeList.add(new Edge(this.id, edgeTokenizer.nextToken()));
                }
                this.setOutEdges(tmpEdgeList);
            }
            else
                throw new IOException("Error while reading. File format not supported.");
        }
    }

    public int compareAllEdges(Vector<Edge> in, Vector<Edge> out) {
        if(this.compareEdges(in) == 0 && this.compareEdges(out) == 0)
            return 0;
        else
            return -1;
    }

    public int compareEdges(Vector<Edge> o) {
        if(this.getInEdges().size() < o.size())
            return -1;
        else if (this.getInEdges().size() > o.size())
            return 1;
        else {
            Iterator thisIter = this.getInEdges().iterator();
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
        boolean isFirst = true;
        StringBuilder result = new StringBuilder();
        // ID
        result.append(this.getId()).append("\t#");
        // IN
        Iterator<Edge> iterator = this.getInEdges().iterator();
        while(iterator.hasNext()) {
            Edge edge = iterator.next();
            if(isFirst) {
                result.append(edge.getSrc());
                isFirst = false;
            } else
                result.append(",").append(edge.getSrc());
        }

        // OUT
        isFirst = true;
        result.append("\t@");
        iterator = this.getOutEdges().iterator();
        if(!iterator.hasNext())
            result.append("\t"); //putting \t so that the readFields won't fail tokenizer("#@") at least "\t" is as an element dividing IN n OUT
        else {
            while(iterator.hasNext()) {
                Edge edge = iterator.next();
                if(isFirst) {
                    result.append(edge.getDest());
                    isFirst = false;
                } else
                    result.append(",").append(edge.getDest());
            }
        }

        return new Text(result.toString());
    }

    public Text toTextConnectedComponent() {
        boolean isFirst = true;
        StringBuilder result = new StringBuilder();
        // ID
        result.append(this.getId()).append("\t").append(this.getId()).append("$").append("\t# ");

        // IN
        Iterator<Edge> iterator = this.getInEdges().iterator();
        while(iterator.hasNext()) {
            Edge edge = iterator.next();
            if(isFirst == true) {
                result.append(edge.getSrc());
                isFirst = false;
            } else
                result.append(", ").append(edge.getSrc());
        }

        // OUT
        isFirst = true;
        result.append("\t@ ");
        iterator = this.getOutEdges().iterator();
        while(iterator.hasNext()) {
            Edge edge = iterator.next();
            if(isFirst == true) {
                result.append(edge.getDest());
                isFirst = false;
            } else
                result.append(", ").append(edge.getDest());
        }

        return new Text(result.toString());
    }

    public DirectedNode copy() {
        DirectedNode newObj = new DirectedNode();
        newObj.setId(this.getId());
        newObj.setInEdges(this.getInEdges());
        newObj.setOutEdges(this.getOutEdges());
        return newObj;
    }

    /*
       Read only ID of the Node
    */
    public static String readNodeId(String nodeLine) throws IOException{
        String nodeID = null;

        if(nodeLine.charAt(0) != '#') {
            StringTokenizer tokenizer = new StringTokenizer(nodeLine, "#@");
            if(tokenizer.countTokens() > 1) {
                // id
                nodeID = new StringTokenizer(tokenizer.nextToken(), "\t").nextToken(); // "id\t"
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

