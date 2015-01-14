package org.tudelft.graphalytics.giraph.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Utility class for parsing the output of Giraph computations that yield a
 * long, long pair.
 *
 * @author Tim Hegeman
 */
public class LongLongJobResultParser {

	private static final Pattern SEPARATOR = Pattern.compile("[\t ]");
	private String filename;
	
	private LongLongJobResultParser(String filename) {
		this.filename = filename;
	}
	
	private Collection<Pair<Long, Long>> parse() throws NumberFormatException, IOException {
		Collection<Pair<Long, Long>> parsed = new ArrayList<>();
		
		// Initialize file input reader
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		
		// Parse each line and add to the output list
		String line;
		while ((line = reader.readLine()) != null) {
			String[] tokens = SEPARATOR.split(line);
			if (tokens.length == 2)
				parsed.add(new Pair<Long, Long>(Long.parseLong(tokens[0]), Long.parseLong(tokens[1])));
		}
		
		reader.close();
		return parsed;
	}
	
	public static Collection<Pair<Long, Long>> parse(String filename) throws NumberFormatException, IOException {
		return new LongLongJobResultParser(filename).parse();
	}
	
}
