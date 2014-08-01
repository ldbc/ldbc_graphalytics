package org.test.giraph.utils.readers.undirected;

import com.google.common.collect.Maps;
import org.apache.giraph.graph.BasicVertex;
import org.apache.giraph.graph.BspUtils;
import org.apache.giraph.lib.TextVertexInputFormat;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;

public class UndirectedFilteredVertexReader extends TextVertexInputFormat.TextVertexReader<VIntWritable, Text, VIntWritable, Text> {
    private static final Logger LOG = Logger.getLogger(UndirectedFilteredVertexReader.class);
    /**
     * Constructor with the line record reader.
     *
     * @param lineRecordReader Will read from this line.
     */
    public UndirectedFilteredVertexReader(
            RecordReader<LongWritable, Text> lineRecordReader) {
        super(lineRecordReader);
    }

    // vertexId     vertexValue  edgeValue    Msg value
    public BasicVertex<VIntWritable, Text, VIntWritable, Text> getCurrentVertex() throws IOException, InterruptedException {
        BasicVertex<VIntWritable, Text, VIntWritable, Text> vertex = BspUtils.<VIntWritable, Text, VIntWritable, Text>createVertex(getContext().getConfiguration());

        Text line = getRecordReader().getCurrentValue();
        Map<VIntWritable, VIntWritable> edges = Maps.newHashMap();

        StringTokenizer tokenizer = new StringTokenizer(line.toString(), " \t,");
        if(tokenizer.countTokens() > 1) {
            // id
            VIntWritable id = new VIntWritable(Integer.parseInt(tokenizer.nextToken())); // "id\t"

            int degreeCounter = 0;

            // store bidirectional-edges
            while(tokenizer.hasMoreElements()) {
                VIntWritable edgeDstId = new VIntWritable(Integer.parseInt(tokenizer.nextToken())); // edge dst
                edges.put(edgeDstId, new VIntWritable(0));
                degreeCounter++;
            }

            vertex.initialize(id, new Text(String.valueOf(degreeCounter)), edges, null);
        }
        else
            throw new IOException("Error while reading. File format not supported.");

        return vertex;
    }

    public boolean nextVertex() throws IOException, InterruptedException {
        return getRecordReader().nextKeyValue();
    }
}
