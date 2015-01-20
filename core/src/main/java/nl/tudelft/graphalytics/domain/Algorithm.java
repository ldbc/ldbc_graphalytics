package nl.tudelft.graphalytics.domain;

import nl.tudelft.graphalytics.domain.algorithms.BreadthFirstSearchParameters.BreadthFirstSearchParametersFactory;
import nl.tudelft.graphalytics.domain.algorithms.CommunityDetectionParameters.CommunityDetectionParametersFactory;
import nl.tudelft.graphalytics.domain.algorithms.EmptyParametersFactory;
import nl.tudelft.graphalytics.domain.algorithms.ForestFireModelParameters.ForestFireModelParametersFactory;
import nl.tudelft.graphalytics.domain.algorithms.ParameterFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An exhaustive enumeration of the algorithms supported by the Graphalytics benchmark suite.
 *
 * @author Tim Hegeman
 */
public enum Algorithm {
	BFS("BFS", "Breadth first search", BreadthFirstSearchParametersFactory.class),
	CD("CD", "Community detection", CommunityDetectionParametersFactory.class),
	CONN("CONN", "Connected components", EmptyParametersFactory.class),
	EVO("EVO", "Forest fire model", ForestFireModelParametersFactory.class),
	STATS("STATS", "Local clustering coefficient", EmptyParametersFactory.class);

	private static final Logger LOG = LogManager.getLogger();

	private final String acronym;
	private final String name;
	private final Class<? extends ParameterFactory<?>> parameterFactoryClass;

	/**
	 * @param acronym               acronym for the algorithm name
	 * @param name                  human-readable name of the algorithm
	 * @param parameterFactoryClass factory class for parsing algorithm parameters
	 */
	Algorithm(String acronym, String name, Class<? extends ParameterFactory<?>> parameterFactoryClass) {
		this.acronym = acronym;
		this.name = name;
		this.parameterFactoryClass = parameterFactoryClass;
	}

	/**
	 * @param acronym the acronym of an algorithm
	 * @return the corresponding Algorithm, or null if it does not exist
	 */
	public static Algorithm fromAcronym(String acronym) {
		if (acronym == null)
			return null;

		String acronymUpperCase = acronym.toUpperCase().trim();
		for (Algorithm algorithm : Algorithm.values()) {
			if (algorithm.getAcronym().equals(acronymUpperCase))
				return algorithm;
		}
		return null;
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

	/**
	 * @return ParameterFactory for this algorithm
	 */
	public ParameterFactory<?> getParameterFactory() {
		try {
			return parameterFactoryClass.newInstance();
		} catch (IllegalAccessException | InstantiationException ex) {
			LOG.error("Failed to instantiate ParameterFactory for algorithm " + getAcronym(), ex);
			return new EmptyParametersFactory();
		}
	}
}
