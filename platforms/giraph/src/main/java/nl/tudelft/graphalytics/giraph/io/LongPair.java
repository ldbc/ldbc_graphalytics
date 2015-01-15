package nl.tudelft.graphalytics.giraph.io;

/**
 * Wrapper class for a pair of longs.
 *
 * @author Tim Hegeman
 */
public class LongPair {

	private long first;
	private long second;
	
	public LongPair(long first, long second) {
		this.first = first;
		this.second = second;
	}
	
	public long getFirst() {
		return first;
	}
	public long getSecond() {
		return second;
	}
	
}
