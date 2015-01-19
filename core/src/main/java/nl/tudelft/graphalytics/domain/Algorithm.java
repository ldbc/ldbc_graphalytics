package nl.tudelft.graphalytics.domain;

/**
 * An exhaustive enumeration of the algorithms supported by the Graphalytics benchmark suite.
 *
 * @author Tim Hegeman
 */
public enum Algorithm {
	BFS("BFS", "Breadth first search"),
	CD("CD", "Community detection"),
	CONN("CONN", "Connected components"),
	EVO("EVO", "Forest fire model"),
	STATS("STATS", "Local clustering coefficient");

	private final String acronym;
	private final String name;

	/**
	 * @param acronym acronym for the algorithm name
	 * @param name    human-readable name of the algorithm
	 */
	Algorithm(String acronym, String name) {
		this.acronym = acronym;
		this.name = name;
	}

	/**
	 * @return acronym for the algorithm name
	 */
	public String getAcronym() {
		return acronym;
	}

	/**
	 * @return human-readable name of the algorithm
	 */
	public String getName() {
		return name;
	}
}
