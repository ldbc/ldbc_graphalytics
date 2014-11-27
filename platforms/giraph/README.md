Giraph needs to be compiled in "pure YARN" mode and installed into a local maven repository.

1. Checkout giraph release-1.1 (commit 0466bccf96ef4c92df4c20a6a4bad62cdf7624c1)
2. Apply "giraph-yarn.patch" found in this directory
3. mvn clean -Phadoop_yarn_latest package install


