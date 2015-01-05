package org.tudelft.graphalytics.giraph.test;

/**
 * Wrapper class for pairs of values of any type.
 *
 * @param <T1> the type of the first value
 * @param <T2> the type of the second value
 *
 * @author Tim Hegeman
 */
public class Pair<T1, T2> {

	private T1 first;
	private T2 second;
	
	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}
	
	public T1 getFirst() {
		return first;
	}
	public T2 getSecond() {
		return second;
	}
	
	@Override
	public String toString() {
		return "<" + first + "," + second + ">";
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Pair))
			return false;
		Pair other = (Pair)obj;
		
		return firstEquals(other) && secondEquals(other);
	}
	
	@SuppressWarnings("rawtypes")
	private boolean firstEquals(Pair other) {
		return (getFirst() == other.getFirst() ||
				(getFirst() != null && getFirst().equals(other.getFirst())));
	}
	
	@SuppressWarnings("rawtypes")
	private boolean secondEquals(Pair other) {
		return (getSecond() == other.getSecond() ||
				(getSecond() != null && getSecond().equals(other.getSecond())));
	}
}
