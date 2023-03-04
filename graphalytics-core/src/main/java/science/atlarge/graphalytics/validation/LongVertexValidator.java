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
package science.atlarge.graphalytics.validation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.duckdb.DuckDBConnection;
import science.atlarge.graphalytics.validation.rule.ValidationRule;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;

/**
 * Class takes the output file generated by a platform for specific algorithm and the reference output for this algorithm.
 * Both files should be in Graphalytics vertex file format. The values of the vertices are validate using the given validation
 * rule.
 *
 * @author Stijn Heldens
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class LongVertexValidator extends VertexValidator {
	private static final Logger LOG = LogManager.getLogger(LongVertexValidator.class);

	final private Path outputDir;
	final private Path validationDir;
	final private ValidationRule rule;
	final private boolean verbose;

	public LongVertexValidator(Path outputPath, Path validationFile, ValidationRule rule, boolean verbose) {
		this.outputDir = outputPath;
		this.validationDir = validationFile;
		this.rule = rule;
		this.verbose = verbose;
	}

	public boolean validate() throws ValidatorException {
		try (DuckDBConnection conn = (DuckDBConnection) DriverManager.getConnection(
				String.format("jdbc:duckdb:%s/../validation.duckdb", outputDir))) {
			Statement stmt = conn.createStatement();

			LOG.info("Validating contents of '" + outputDir + "'...");
			parseFileOrDirectory(validationDir, "expected", conn);
			parseFileOrDirectory(outputDir, "actual", conn);

			// number of results
			if (!compareNumberOfVertices(conn, LOG)) {
				return false;
			}

			// vertices
			if (!compareVertexIds(conn, LOG, verbose)) {
				return false;
			}

			// values
			ResultSet rs = stmt.executeQuery(rule.getQuery());
			boolean valid = true;
			long i = 0;
			while (rs.next()) {
				valid = false;
				i++;
				if (verbose && i <= MAX_PRINT_ERROR_COUNT) {
					LOG.error(String.format("Validation failed for %d: expected: %d, actual: %d",
							rs.getLong(1),
							rs.getLong(2),
							rs.getLong(3)));
				}
			}
			rs.close();
			stmt.close();
			return valid;
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			throw new ValidatorException("Validation error", e);
		}
	}

	private void parseFileOrDirectory(final Path filePath, final String tableName, final Connection conn) throws IOException {
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(String.format("CREATE OR REPLACE TABLE %s(v BIGINT NOT NULL, x BIGINT NOT NULL);", tableName));
			stmt.close();
		} catch (SQLException e) {
			throw new IOException(e);
		}

		LOG.info(String.format("Parsing file/directory %s.", filePath));
		Files.walkFileTree(filePath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				try {
					Statement stmt = conn.createStatement();
					stmt.execute(String.format("COPY %s FROM '%s' (DELIMITER ' ', FORMAT csv)", tableName, file.toAbsolutePath()));
					stmt.close();
				} catch (SQLException e) {
					throw new IOException(e);
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
