package org.tudelft.graphalytics.mapreduceutils.io;

/**
 * Collection of MapReduce Counters used for providing information to users,
 * and for debugging input parsing.
 */
public class Counters {

	public enum ParseErrors {
		NUMBER_FORMAT_EXCEPTION,
		INVALID_LINE_FORMAT
	}
	
}
