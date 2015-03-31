package nl.tudelft.graphalytics.mapreducev2.stats;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.Iterator;

/**
 * Combiner for values of the DoubleAverage class. Used by the STATS algorithm to aggregate the mean local clustering
 * coefficient.
 *
 * @author Tim Hegeman
 */
public class DoubleAverageCombiner extends MapReduceBase implements Reducer<Text, DoubleAverage, Text, DoubleAverage> {

	@Override
	public void reduce(Text key, Iterator<DoubleAverage> values, OutputCollector<Text, DoubleAverage> output,
			Reporter reporter) throws IOException {
		DoubleAverage average = new DoubleAverage();
		while (values.hasNext()) {
			average.add(values.next());
		}
		output.collect(key, average);
	}
}
