package org.tudelft.graphalytics.giraph.stats;

import org.apache.giraph.master.DefaultMasterCompute;

/**
 * Master computation for the local clustering coefficient algorithm, which
 * registers the DoubleAverage aggregator to yield a graph-wide average LCC.
 *
 * @author Tim Hegeman
 */
public class StatsMasterComputation extends DefaultMasterCompute {

	public static final String LCC_AGGREGATOR_NAME = "Average LCC";
	
	@Override
	public void compute() {
	}

	@Override
	public void initialize() throws InstantiationException,
			IllegalAccessException {
		registerPersistentAggregator(LCC_AGGREGATOR_NAME, DoubleAverageAggregator.class);
	}

}
