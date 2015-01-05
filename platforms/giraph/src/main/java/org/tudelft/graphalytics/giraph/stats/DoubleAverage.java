package org.tudelft.graphalytics.giraph.stats;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

/**
 * Wrapper class for computing the average of a sequence of doubles.
 *
 * @author Tim Hegeman
 */
public class DoubleAverage implements Writable {

	private double sum;
	private long count;
	
	/**
	 * Needed for the Writable interface. Do not use.
	 */
	public DoubleAverage() {
		this.sum = 0.0;
		this.count = 0;
	}
	
	/**
	 * @param value a single value.
	 */
	public DoubleAverage(double value) {
		this.sum = value;
		this.count = 1;
	}
	
	/**
	 * Add another DoubleAverage to this one to yield the
	 * overall (weighted) average of both.
	 * 
	 * @param other the DoubleAverage to add
	 */
	public void add(DoubleAverage other) {
		this.sum += other.sum;
		this.count += other.count;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeDouble(sum);
		out.writeLong(count);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		sum = in.readDouble();
		count = in.readLong();
	}
	
	@Override
	public String toString() {
		return (count == 0 ? "0" : Double.toString(sum / count));
	}

}
