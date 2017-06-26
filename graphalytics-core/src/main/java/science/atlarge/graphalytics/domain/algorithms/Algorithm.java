/*
 * Copyright 2015 - 2017 Atlarge Research Team,
 * operating at Technische Universiteit Delft
 * and Vrije Universiteit Amsterdam, the Netherlands.
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
package science.atlarge.graphalytics.domain.algorithms;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import science.atlarge.graphalytics.domain.algorithms.EmptyParameters.EmptyParametersFactory;
import science.atlarge.graphalytics.validation.rule.EpsilonValidationRule;
import science.atlarge.graphalytics.validation.rule.EquivalenceValidationRule;
import science.atlarge.graphalytics.validation.rule.MatchLongValidationRule;
import science.atlarge.graphalytics.validation.rule.ValidationRule;

/**
 * An exhaustive enumeration of the algorithms supported by the Graphalytics benchmark suite.
 *
 * @author Mihai Capotă
 * @author Stijn Heldens
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public enum Algorithm {
	// Graphalytics core algorithms
	BFS("BFS", "Breadth first search", BreadthFirstSearchParameters.BreadthFirstSearchParametersFactory.class, MatchLongValidationRule.class),
	CDLP("CDLP", "Community detection - label propagation", CommunityDetectionLPParameters.CommunityDetectionLPParametersFactory.class, EquivalenceValidationRule.class),
	LCC("LCC", "Local clustering coefficient", EmptyParametersFactory.class, EpsilonValidationRule.class),
	PR("PR", "PageRank", PageRankParameters.PageRankParametersFactory.class, EpsilonValidationRule.class),
	SSSP("SSSP", "Single source shortest paths", SingleSourceShortestPathsParameters.SingleSourceShortestPathsParametersFactory.class, EpsilonValidationRule.class),
	WCC("WCC", "Connected components", EmptyParametersFactory.class, EquivalenceValidationRule.class),

	// Previously supported algorithms
	FFM("FFM", "Forest fire model", ForestFireModelParameters.ForestFireModelParametersFactory.class, MatchLongValidationRule.class);

	private static final Logger LOG = LogManager.getLogger();

	private final String acronym;
	private final String name;
	private final Class<? extends ParameterFactory<?>> parameterFactoryClass;
	private final Class<? extends ValidationRule<?>> validationRuleClass;

	/**
	 * @param acronym               acronym for the algorithm name
	 * @param name                  human-readable name of the algorithm
	 * @param parameterFactoryClass factory class for parsing algorithm parameters
	 */
	Algorithm(String acronym, String name, Class<? extends ParameterFactory<?>> parameterFactoryClass, Class<? extends ValidationRule<?>> validationRuleClass) {
		this.acronym = acronym;
		this.name = name;
		this.parameterFactoryClass = parameterFactoryClass;
		this.validationRuleClass = validationRuleClass;
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


	/**
	 * @return ValidationRule for this algorithm
	 */
	public ValidationRule<?> getValidationRule() {
		try {
			return validationRuleClass.newInstance();
		} catch (IllegalAccessException | InstantiationException ex) {
			LOG.error("Failed to instantiate ValidationRule for algorithm " + getAcronym(), ex);
			throw new RuntimeException(ex);
		}
	}
}
