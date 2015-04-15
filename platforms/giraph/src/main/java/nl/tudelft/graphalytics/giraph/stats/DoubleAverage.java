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

	/**
	 * @return the average value of all added values
	 */
	public double get() {
		if (count == 0)
			return 0.0;
		else
			return sum / count;
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
