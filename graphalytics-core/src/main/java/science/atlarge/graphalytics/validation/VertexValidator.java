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

import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Stijn Heldens
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public abstract class VertexValidator<E> {

    public static final long MAX_PRINT_ERROR_COUNT = 100;
    public abstract boolean validate() throws ValidatorException;

    protected boolean compareNumberOfVertices(Connection conn, Logger LOG) throws SQLException {
        long expectedCount = getCountVertices(conn, "expected");
        long actualCount = getCountVertices(conn, "actual");

        if (actualCount != expectedCount) {
            LOG.error(String.format("Vertex count is incorrect, expected: %d, actual: %d", expectedCount, actualCount));
            return false;
        } else {
            return true;
        }
    }

    private long getCountVertices(Connection conn, String table) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(String.format("SELECT count(*) AS vertexCount FROM %s;", table));
        rs.next();
        long count = rs.getLong(1);
        stmt.close();
        return count;
    }

    protected boolean compareVertexIds(Connection conn, Logger LOG, boolean verbose) throws SQLException {
        return createDifferenceOfTables(conn, LOG, "actual", "expected", verbose)
            || createDifferenceOfTables(conn, LOG, "expected", "actual", verbose);
    }

    private boolean createDifferenceOfTables(Connection conn, Logger LOG, String table1, String table2, boolean verbose) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(String.format("SELECT v FROM %s EXCEPT SELECT v FROM %s", table1, table2));
        boolean valid = true;
        int i = 0;
        while (rs.next()) {
            valid = false;
            i++;
            if (verbose && i <= MAX_PRINT_ERROR_COUNT) {
                LOG.error(String.format("Validation failed: Vertex %d found in %s vertex set but not found in %s vertex set",
                        rs.getLong(1), table1, table2));
            }
        }
        rs.close();
        stmt.close();
        return valid;
    }


}
