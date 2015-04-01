package nl.tudelft.graphalytics.mapreducev2.stats;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Tim Hegeman
 */
public class StatsCCReducer extends MapReduceBase implements Reducer<Text, DoubleAverage, NullWritable, Text> {

	public void reduce(Text key, Iterator<DoubleAverage> values,
			OutputCollector<NullWritable, Text> output, Reporter reporter) throws IOException {
		DoubleAverage ccAverage = new DoubleAverage();
		while (values.hasNext()) {
			ccAverage.add(values.next());
		}

		output.collect(null, new Text(key.toString() + " " + ccAverage.get()));
	}
}


