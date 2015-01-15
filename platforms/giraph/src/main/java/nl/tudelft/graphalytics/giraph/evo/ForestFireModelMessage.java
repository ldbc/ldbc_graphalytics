package nl.tudelft.graphalytics.giraph.evo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

/**
 * Message class for the forest fire model algorithm. Wraps the multiple types
 * of messages that are sent throughout the algorithm.
 */
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

	/**
	 * Note: This returns a sane value for any message type except BURNING_NOTIFICATION.
	 *
	 * @return the source of the message
	 */
	public long getSourceId() {
		return sourceId;
	}

	/**
	 * Note: This returns a sane value for any message type except NEIGHBOUR_NOTIFICATION.
	 *
	 * @return the instigator described by this message
	 */
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

	/**
	 * @param sourceId the source of the message
	 * @return a new message of the NEIGHBOUR_NOTIFICATION type
	 */
	public static ForestFireModelMessage neighbourNotification(long sourceId) {
		return new ForestFireModelMessage(Type.NEIGHBOUR_NOTIFICATION).withSourceId(sourceId);
	}

	/**
	 * @param sourceId the source of this message
	 * @param instigatorId the instigator to request liveness for
	 * @return a new message of the LIVENESS_REQUEST type
	 */
	public static ForestFireModelMessage livenessRequest(long sourceId, long instigatorId) {
		return new ForestFireModelMessage(Type.LIVENESS_REQUEST, sourceId, instigatorId);
	}

	/**
	 * @param sourceId the source of this message
	 * @param instigatorId the instigator to confirm liveness for
	 * @return a new message of the ALIVE_ACKNOWLEDGEMENT type
	 */
	public static ForestFireModelMessage aliveAcknowledgement(long sourceId, long instigatorId) {
		return new ForestFireModelMessage(Type.ALIVE_ACKNOWLEDGEMENT, sourceId, instigatorId);
	}

	/**
	 * @param instigatorId the instigator that is now burning the destination node
	 * @return a new message of the BURNING_NOTIFICATION type
	 */
	public static ForestFireModelMessage burningNotification(long instigatorId) {
		return new ForestFireModelMessage(Type.BURNING_NOTIFICATION).withInstigatorId(instigatorId);
	}
	
}
