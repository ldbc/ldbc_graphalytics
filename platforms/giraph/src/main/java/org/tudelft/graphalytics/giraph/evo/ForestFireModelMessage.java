package org.tudelft.graphalytics.giraph.evo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class ForestFireModelMessage implements Writable {

	public static enum Type {
		NEIGHBOUR_NOTIFICATION,
		LIVENESS_REQUEST,
		ALIVE_ACKNOWLEDGEMENT,
		BURNING_NOTIFICATION
	}
	
	// The type of message
	private Type type;
	// [NEIGHBOUR_NOTIFICATION,LIVENESS_REQUEST,ALIVE_ACKNOWLEDGEMENT]
	// The ID of the vertex sending the message.
	private long sourceId;
	// [LIVENESS_REQUEST,ALIVE_ACKNOWLEDGEMENT,BURNING_NOTIFICATION]
	private long instigatorId;

	/** Required for instantiation using the Writable interface. Do not use. */
	public ForestFireModelMessage() {
	}
	
	private ForestFireModelMessage(Type type) {
		this.type = type;
	}
	private ForestFireModelMessage(Type type, long sourceId, long instigatorId) {
		this.type = type;
		this.sourceId = sourceId;
		this.instigatorId = instigatorId;
	}
	
	private ForestFireModelMessage withSourceId(long sourceId) {
		this.sourceId = sourceId;
		return this;
	}
	private ForestFireModelMessage withInstigatorId(long instigatorId) {
		this.instigatorId = instigatorId;
		return this;
	}
	
	public long getSourceId() {
		return sourceId;
	}
	public long getInstigatorId() {
		return instigatorId;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeByte(type.ordinal());
		switch (type) {
		case NEIGHBOUR_NOTIFICATION:
			out.writeLong(sourceId);
			break;
		case LIVENESS_REQUEST:
		case ALIVE_ACKNOWLEDGEMENT:
			out.writeLong(sourceId);
			out.writeLong(instigatorId);
			break;
		case BURNING_NOTIFICATION:
			out.writeLong(instigatorId);
			break;
		}
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		type = Type.values()[in.readByte()];
		switch (type) {
		case NEIGHBOUR_NOTIFICATION:
			sourceId = in.readLong();
			break;
		case LIVENESS_REQUEST:
		case ALIVE_ACKNOWLEDGEMENT:
			sourceId = in.readLong();
			instigatorId = in.readLong();
			break;
		case BURNING_NOTIFICATION:
			instigatorId = in.readLong();
			break;
		}
	}
	
	public static ForestFireModelMessage neighbourNotification(long sourceId) {
		return new ForestFireModelMessage(Type.NEIGHBOUR_NOTIFICATION).withSourceId(sourceId);
	}
	
	public static ForestFireModelMessage livenessRequest(long sourceId, long instigatorId) {
		return new ForestFireModelMessage(Type.LIVENESS_REQUEST, sourceId, instigatorId);
	}
	
	public static ForestFireModelMessage aliveAcknowledgement(long sourceId, long instigatorId) {
		return new ForestFireModelMessage(Type.ALIVE_ACKNOWLEDGEMENT, sourceId, instigatorId);
	}

	public static ForestFireModelMessage burningNotification(long instigatorId) {
		return new ForestFireModelMessage(Type.BURNING_NOTIFICATION).withInstigatorId(instigatorId);
	}
	
}
