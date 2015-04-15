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
package nl.tudelft.graphalytics.giraph.evo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.io.Writable;

/**
 * Vertex data for the forest fire model algorithm. Include a list of incoming edges
 * (which is not provided by Giraph), and a set of states to track which "instigators"
 * have reached this node.
 *
 * @author Tim Hegeman
 */
public class ForestFireModelData implements Writable {

	public static enum ForestFireModelState {
		ALIVE,
		BURNING,
		BURNED
	}
	
	private long[] inEdges;
	private Map<Long, ForestFireModelState> statePerInstigator;
	
	/** Required for instantiation using the Writable interface. Do not use. */
	public ForestFireModelData() {
		inEdges = new long[0];
		statePerInstigator = new HashMap<>();
	}
	
	private ForestFireModelData(long[] inEdges) {
		this.inEdges = inEdges;
		this.statePerInstigator = new HashMap<>();
	}

	/**
	 * @return list of node IDs from incoming edges
	 */
	public long[] getInEdges() {
		return inEdges;
	}

	/**
	 * @return set of instigator and state pairs
	 */
	public Set<Map.Entry<Long, ForestFireModelState>> getStates() {
		return statePerInstigator.entrySet();
	}

	/**
	 * @param instigatorId the instigator to retrieve a state for
	 * @return the state of this node for the given instigator
	 */
	public ForestFireModelState getState(long instigatorId) {
		if (statePerInstigator.containsKey(instigatorId))
			return statePerInstigator.get(instigatorId);
		return ForestFireModelState.ALIVE;
	}
	/**
	 * @return the set of instigators with a known state in this node
	 */
	public Set<Long> getInstigatorIds() {
		return statePerInstigator.keySet();
	}

	/**
	 * @param instigatorId an instigator whose state to change at this node
	 * @param newState the new state for the instigator
	 */
	public void setState(long instigatorId, ForestFireModelState newState) {
		statePerInstigator.put(instigatorId, newState);
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		// Store the incoming edges of this vertex
		out.writeInt(inEdges.length);
		for (int i = 0; i < inEdges.length; i++) {
			out.writeLong(inEdges[i]);
		}
		// Store the forest fire state of this vertex with respect to various sources
		out.writeInt(statePerInstigator.size());
		for (Map.Entry<Long, ForestFireModelState> state : statePerInstigator.entrySet()) {
			out.writeLong(state.getKey());
			out.writeByte(state.getValue().ordinal());
		}
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// Read the incoming edges
		int length = in.readInt();
		inEdges = new long[length];
		for (int i = 0; i < length; i++) {
			inEdges[i] = in.readLong();
		}
		// Read the forest fire states
		length = in.readInt();
		statePerInstigator = new HashMap<>();
		for (int i = 0; i < length; i++) {
			long id = in.readLong();
			ForestFireModelState state = ForestFireModelState.values()[in.readByte()];
			statePerInstigator.put(id, state);
		}
	}

	/**
	 * @param inEdges the set of incoming edges of a particular node
	 * @return a new ForestFireModelData object
	 */
	public static ForestFireModelData fromInEdges(Collection<Long> inEdges) {
		long[] inEdgeArray = new long[inEdges.size()];
		int i = 0;
		for (Long inEdge : inEdges)
			inEdgeArray[i++] = inEdge;
		return new ForestFireModelData(inEdgeArray);
	}
	
}
