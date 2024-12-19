# Matching using SQL

SQL implementation of the various matching strategies described in [the specification](https://arxiv.org/pdf/2011.15028.pdf#page=15).

## Schema

```sql
CREATE OR REPLACE TABLE exact_expected      (v BIGINT NOT NULL, x DOUBLE NOT NULL);
CREATE OR REPLACE TABLE exact_actual        (v BIGINT NOT NULL, x DOUBLE NOT NULL);
CREATE OR REPLACE TABLE epsilon_expected    (v BIGINT NOT NULL, x DOUBLE NOT NULL);
CREATE OR REPLACE TABLE epsilon_actual      (v BIGINT NOT NULL, x DOUBLE NOT NULL);
CREATE OR REPLACE TABLE equivalence_expected(v BIGINT NOT NULL, x BIGINT NOT NULL);
CREATE OR REPLACE TABLE equivalence_actual  (v BIGINT NOT NULL, x BIGINT NOT NULL);
```

## Loading the data

```sql
COPY exact_expected       FROM 'exact_expected.csv'       (DELIMITER ' ', FORMAT csv);
COPY epsilon_expected     FROM 'epsilon_expected.csv'     (DELIMITER ' ', FORMAT csv);
COPY equivalence_expected FROM 'equivalence_expected.csv' (DELIMITER ' ', FORMAT csv);
```

The `*_actual1.csv` files should pass validation:

```sql
COPY exact_actual         FROM 'exact_actual1.csv'        (DELIMITER ' ', FORMAT csv);
COPY epsilon_actual       FROM 'epsilon_actual1.csv'      (DELIMITER ' ', FORMAT csv);
COPY equivalence_actual   FROM 'equivalence_actual1.csv'  (DELIMITER ' ', FORMAT csv);
```

The `*_actual2.csv` files should not pass validation:

```sql
COPY exact_actual         FROM 'exact_actual2.csv'        (DELIMITER ' ', FORMAT csv);
COPY epsilon_actual       FROM 'epsilon_actual2.csv'      (DELIMITER ' ', FORMAT csv);
COPY equivalence_actual   FROM 'equivalence_actual2.csv'  (DELIMITER ' ', FORMAT csv);
```

## Comparison scripts

These scripts find the _violations_ of the respective equivalences.

### Exact comparison script

```sql
SELECT exact_expected.v AS v, exact_expected.x AS expected, exact_actual.x AS actual
FROM exact_expected, exact_actual
WHERE exact_expected.v = exact_actual.v
  AND exact_expected.x != exact_actual.x
LIMIT 100;
```

### Epsilon comparison script

```sql
SELECT epsilon_expected.v AS v, epsilon_expected.x AS expected, epsilon_actual.x AS actual
FROM epsilon_expected, epsilon_actual
WHERE epsilon_expected.v = epsilon_actual.v
  AND NOT
    CASE
        WHEN (epsilon_expected.x = 'Infinity' AND epsilon_actual.x = 'Infinity') THEN true
        WHEN (epsilon_expected.x = 'Infinity' AND epsilon_actual.x != 'Infinity') THEN false
        WHEN (epsilon_expected.x != 'Infinity' AND epsilon_actual.x = 'Infinity') THEN false
        WHEN abs(epsilon_expected.x - epsilon_actual.x) < 0.0001 * epsilon_expected.x THEN true
        ELSE false
    END
LIMIT 100;
```

### Equivalence comparison script

```sql
SELECT e1.v AS v, e1.x AS expected, a1.x AS actual
FROM equivalence_expected e1, equivalence_actual a1
WHERE e1.v = a1.v -- select a node in the expected-actual tables
  AND (
    EXISTS (
      SELECT 1
      FROM equivalence_expected e2, equivalence_actual a2
      WHERE e2.v  = a2.v -- another node which occurs in both the 'expected' and the 'actual' tables,
        AND e1.x  = e2.x -- where the node is in the same equivalence class in the 'expected' table
        AND a1.x != a2.x -- but in a different one in the 'actual' table
      LIMIT 1            -- finding a single counterexample is sufficient
    ) OR
    EXISTS (
      FROM equivalence_expected e2, equivalence_actual a2
      WHERE e2.v  = a2.v -- another node which occurs in both the 'expected' and the 'actual' tables,
        AND a1.x  = a2.x -- where the node is in the same equivalence class in the 'actual' table
        AND e1.x != e2.x -- but in a different one in the 'expected' table
      LIMIT 1            -- finding a single counterexample is sufficient
    )
  )
LIMIT 100;
```
