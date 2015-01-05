package org.tudelft.graphalytics.giraph.stats;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

/**
 * Message class representing the various types of messages sent in the LCC algorithm.
 *
 * @author Tim Hegeman
 */
public class StatsMessage implements Writable {

	private long source;
	private long destination;
	
	/**
	 * Used for acknowledging the existence of an edge. Because the existence of
	 * the message is enough to indicate that one of the requested edges exist,
	 * we can just count the number of messages and do not care about content. 
	 */
	public StatsMessage() {
		this.source = this.destination = 0;
	}
	
	/**
	 * Used for informing neighbours of the existence of an incoming edge
	 * (directed graphs only).
	 * 
	 * @param source the source vertex ID.
	 */
	public StatsMessage(long source) {
		this.source = source;
		this.destination = 0;
	}
	
	/**
	 * Used for requesting information about the existence of some edge (between
	 * the recipient of the message and destination) while expecting an answer
	 * to be sent back to source.
	 * 
	 * @param source the source vertex ID.
	 * @param destination the destination vertex ID of the edge we wish to
	 * 	know the existence of.
	 */
	public StatsMessage(long source, long destination) {
		this.source = source;
		this.destination = destination;
	}
	
	public long getSource() {
		return source;
	}
	public long getDestination() {
		return destination;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeLong(source);
		out.writeLong(destination);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		source = in.readLong();
		destination = in.readLong();
	}

}
