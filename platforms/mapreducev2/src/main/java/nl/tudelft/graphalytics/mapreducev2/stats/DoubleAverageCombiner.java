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
