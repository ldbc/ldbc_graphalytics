package org.tudelft.graphalytics.mapreducev2.common;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.StringTokenizer;

public class Edge implements WritableComparable<Edge>{
    private String src, dest;
    private final char ignoreChar = '#';

    public Edge() {}

    public Edge(String src, String dest) {
        //this.id = id;
        this.src = src;
        this.dest = dest;
    }

    public String getSrc() { return src; }
    public void setSrc(String src) { this.src = src; }

    public String getDest() { return dest; }
    public void setDest(String dest) { this.dest = dest; }

    public int compareTo(Edge o) {
        return (this.getSrc().compareTo(o.getSrc()) != 0)
            ? this.getSrc().compareTo(o.getSrc())
            : this.getDest().compareTo(o.getDest());
    }

    public void write(DataOutput dataOutput) throws IOException {
        String edgeAsString = this.toString();
        dataOutput.writeBytes(edgeAsString);
    }

    public void readFields(DataInput input) throws IOException {
        String line = input.readLine();

        if(line.charAt(0) != this.ignoreChar) {
            StringTokenizer tokenizer = new StringTokenizer(line, " \t\n\r\f,.:;?![]'");
            if(tokenizer.countTokens() == 2) {
                this.setSrc(tokenizer.nextToken());
                this.setDest(tokenizer.nextToken());
            }
            else
                throw new IOException("Error while reading. File format not supported.");
        }
    }

    public String toString(){
        return this.getSrc()+","+this.getDest();
    }

    public Edge swapEdge() {
        return new Edge(this.getDest(), this.getSrc());
    }
}
