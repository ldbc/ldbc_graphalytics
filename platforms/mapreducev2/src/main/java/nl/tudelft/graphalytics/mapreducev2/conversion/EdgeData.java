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
package nl.tudelft.graphalytics.mapreducev2.conversion;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

/**
 * Edge specification, with one endpoint (the other endpoint is the MapReduce key) and a direction.
 *
 * @author Tim Hegeman
 */
public class EdgeData implements Writable {

	private long targetId;
	private boolean outgoing;
	
	public EdgeData() {
	}
	public EdgeData(long targetId, boolean outgoing) {
		this.targetId = targetId;
		this.outgoing = outgoing;
	}
	
	public boolean isOutgoing() {
		return outgoing;
	}
	public long getTargetId() {
		return targetId;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeLong(targetId);
		out.writeBoolean(outgoing);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		targetId = in.readLong();
		outgoing = in.readBoolean();
	}

}
