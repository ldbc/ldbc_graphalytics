package nl.tudelft.graphalytics.validation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.tudelft.graphalytics.validation.rule.ValidationRule;

public class VertexValidator<E> {
	private static final Logger LOG = LogManager.getLogger(VertexValidator.class);
	private static final long MAX_PRINT_ERROR_COUNT = 100;

	final private String outputFile;
	final private String validationFile;
	final private ValidationRule<E> rule;
	final private boolean verbose;

	public VertexValidator(String outputFile, String validationFile, ValidationRule rule, boolean verbose) {
		this.outputFile = outputFile;
		this.validationFile = validationFile;
		this.rule = rule;
		this.verbose = verbose;
	}

	public boolean execute() throws ValidatorException {
		Map<Long, E> validationResults, outputResults;

		if (verbose) {
			LOG.info("Validating contents of '" + outputFile + "'...");
		}

		try {
			validationResults = parseFile(validationFile);
		} catch (IOException e) {
			LOG.warn("Failed to read validation file '" + validationFile + "'");
			return false;
		}

		try {
			outputResults = parseFile(outputFile);
		} catch (IOException e) {
			LOG.warn("Failed to read output file '" + outputFile + "'");
			return false;
		}

		ArrayList<Long> keys = new ArrayList<Long>();
		keys.addAll(validationResults.keySet());
		keys.addAll(outputResults.keySet());
		Collections.sort(keys);

		long errorsCount = 0;

		long missingVertices = 0;
		long unknownVertices = 0;
		long incorrectVertices = 0;
		long correctVertices = 0;

		long prevId = -1;
		for (Long id: keys) {

			// Keep track of previous ID to skip duplicate IDs. Since the
			// list of vertex IDs is sorted, duplicate IDs will be continuous.
			if (prevId == id) {
				continue;
			} else {
				prevId = id;
			}


			String error = null;
			E outputValue = outputResults.get(id);
			E correctValue = validationResults.get(id);

			if (outputValue == null) {
				missingVertices++;
				error = "Vertex " + id + " is missing";
			} else if (correctValue == null) {
				unknownVertices++;
				error = "Vertex " + id + " is not a valid vertex";
			} else if (!rule.match(outputValue, correctValue)) {
				incorrectVertices++;
				error = "Vertex " + id + " has value '" + outputValue + "', but it should be '" + correctValue + "'";
			} else {
				correctVertices++;
			}

			if (error != null) {
				if (verbose && errorsCount < MAX_PRINT_ERROR_COUNT) {
					LOG.info(" - " + error);
				}

				errorsCount++;
			}
		}

		if (errorsCount >= MAX_PRINT_ERROR_COUNT) {
			LOG.info(" - [" + (errorsCount - MAX_PRINT_ERROR_COUNT) + " errors have been omitted] ");
		}

		if (errorsCount > 0) {
			LOG.info("Validation failed");

			long totalVertices = correctVertices + incorrectVertices + missingVertices;

			LOG.info(String.format(" - Correct vertices: %d (%.2f%%)",
					correctVertices, (100.0 * correctVertices) / totalVertices));
			LOG.info(String.format(" - Incorrect vertices: %d (%.2f%%)",
					incorrectVertices, (100.0 * incorrectVertices) / totalVertices));
			LOG.info(String.format(" - Missing vertices: %d (%.2f%%)",
					missingVertices, (100.0 * missingVertices) / totalVertices));
		} else {
			LOG.info("Validation successful");
		}

		return errorsCount == 0;
	}

	private Map<Long, E> parseFile(String file) throws IOException {
		HashMap<Long, E> results = new HashMap<Long, E>();

		try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();

				if (line.isEmpty()) {
					continue;
				}

				String[] parts = line.split("\\s+", 2);

				try {
					long vertexId = Long.parseLong(parts[0]);
					E vertexValue = rule.parse(parts.length > 1 ? parts[1] : "");
					results.put(vertexId,  vertexValue);
				} catch(NumberFormatException e) {
					LOG.warn("Skipping invalid line '" + line + "' of file '" + file + "'");
				}
			}
		}

		return results;
	}
}
