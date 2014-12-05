package org.tudelft.graphalytics.giraph.stats;

import org.apache.giraph.aggregators.BasicAggregator;

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
