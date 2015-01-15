package org.tudelft.graphalytics.mapreducev2.stats;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * @author Marcin Biczak
 */
public class StatsCCContainer implements WritableComparable<StatsCCContainer> {
    private final char ignoreChar = '#';

    private int nodesNr;
    private int edgesNr;
    private double cc;

    public StatsCCContainer() {}
    public StatsCCContainer(int nodesNr, int edgesNr, double cc) {
        this.edgesNr = edgesNr;
        this.cc = cc;
        this.nodesNr = nodesNr;
    }

    public int getEdgesNr() { return edgesNr; }
    public void setEdgesNr(int edgesNr) { this.edgesNr = edgesNr; }

    public double getCc() { return cc; }
    public void setCc(double cc) { this.cc = cc; }

    public int getNodesNr() { return nodesNr; }
    public void setNodesNr(int nodesNr) { this.nodesNr = nodesNr; }

    public int compareTo(StatsCCContainer o) {
        if(new Double(this.getCc()).compareTo(o.getCc()) != 0)
            return new Double(this.getCc()).compareTo(o.getCc());
        else if(new Integer(this.getEdgesNr()).compareTo(o.getEdgesNr()) != 0)
            return new Integer(this.getEdgesNr()).compareTo(o.getEdgesNr());
        else if(new Integer(this.getNodesNr()).compareTo(o.getNodesNr()) != 0)
            return new Integer(this.getNodesNr()).compareTo(o.getNodesNr());
        else
            return 0;
    }

    public void write(DataOutput dataOutput) throws IOException {
        String edgeAsString = this.toString();
        dataOutput.writeBytes(edgeAsString);
    }

    public void readFields(DataInput input) throws IOException {
        this.setCc(Float.parseFloat(this.processLineToken(input.readLine())));
        this.setEdgesNr(Integer.parseInt(this.processLineToken(input.readLine())));
        this.setNodesNr(Integer.parseInt(this.processLineToken(input.readLine())));
    }

    private String processLineToken(String line) throws IOException{
        String result = "";
         if(line.charAt(0) != this.ignoreChar) {
            StringTokenizer tokenizer = new StringTokenizer(line, " \t\n\r\f,:;?![]'");
            if(tokenizer.countTokens() == 4) {
                for(int i=0; i<3; i++) {tokenizer.nextToken();}
                result = tokenizer.nextToken();
            }
            else {
                System.out.println("tokenCount = "+tokenizer.countTokens());
                System.out.println("line = "+line);
                throw new IOException("Error while reading. File format not supported.");
            }
        }
        return result;
    }

    public String toString(){
        return "graph average cc: "+this.getCc()+" \n" +
               "number of edges: "+this.getEdgesNr()+" \n"+
               "number of nodes: "+this.getNodesNr()+"\n";
    }
}