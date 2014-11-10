package org.tudelft.graphalytics.yarn.conn;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.StringTokenizer;

public class LabelDirectedConnectedComponentsMap extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
    private String id;
    private String label;
    private String[] in;
    private String[] out;

    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
        String neighbours = "# ";
        this.readNode(value.toString(), reporter);

        output.collect(new Text(this.id), new Text(this.label));
        for(int i=0; i<this.in.length; i++) {
            output.collect(new Text(this.in[i]), new Text(this.label));
            if(i == 0)
                neighbours += this.in[i];
            else
                neighbours += ","+this.in[i];

            //report progress
            if(i % 1000 == 0) reporter.progress();
        }

        reporter.progress();

        neighbours += "\t@ ";
        for(int i=0; i<this.out.length; i++) {
            output.collect(new Text(this.out[i]), new Text(this.label));
            if(i == 0)
                neighbours += this.out[i];
            else
                neighbours += ","+this.out[i];

            //report progress
            if(i % 1000 == 0) reporter.progress();
        }

        output.collect(new Text(this.id), new Text("$" + neighbours));
    }

    public void readNode(String line, Reporter reporter) throws IOException {
        StringTokenizer basicTokenizer = new StringTokenizer(line, "\t$");
        if(basicTokenizer.countTokens() == 4) {
            this.id = basicTokenizer.nextToken();
            this.label = basicTokenizer.nextToken();
            String inNeigh = basicTokenizer.nextToken();
            String outNeigh = basicTokenizer.nextToken();

            // IN
            StringTokenizer neighTokenizer = new StringTokenizer(inNeigh,", #");
            this.in = new String[neighTokenizer.countTokens()];
            int i=0;
            while (neighTokenizer.hasMoreTokens()) {
                String token = neighTokenizer.nextToken();
                if(token.equals("#")) continue;
                this.in[i] = token;

                if(i % 1000 == 0) reporter.progress();
    
                i++;
            }

            reporter.progress();

            // OUT
            neighTokenizer = new StringTokenizer(outNeigh,", @");
            this.out = new String[neighTokenizer.countTokens()];
            i=0;
            while (neighTokenizer.hasMoreTokens()) {
                String token = neighTokenizer.nextToken();
                if(token.equals("@")) continue;
                this.out[i] = token;

                if(i % 1000 == 0) reporter.progress();

                i++;
            }
        } else {
            throw new IOException("ConnCompRecord requires 4 basicTokens as patter got "+basicTokenizer.countTokens());
        }
    }
}
