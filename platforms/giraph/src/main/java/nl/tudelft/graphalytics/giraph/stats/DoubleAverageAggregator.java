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
package nl.tudelft.graphalytics.giraph.stats;

import org.apache.giraph.aggregators.BasicAggregator;

/**
 * Aggregator for DoubleAverage values. Used to aggregate the clustering coefficients
 * of individual vertices into a graph-wide average value.
 *
 * @author Tim Hegeman
 */
public class DoubleAverageAggregator extends BasicAggregator<DoubleAverage> {

	@Override
	public void aggregate(DoubleAverage value) {
		getAggregatedValue().add(value);
	}

	@Override
	public DoubleAverage createInitialValue() {
		return new DoubleAverage();
	}

}
