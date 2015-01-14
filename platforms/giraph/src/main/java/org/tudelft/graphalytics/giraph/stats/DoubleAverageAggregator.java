package org.tudelft.graphalytics.giraph.stats;

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
