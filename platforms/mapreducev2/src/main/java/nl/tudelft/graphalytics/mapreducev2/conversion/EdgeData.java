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
