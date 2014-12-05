# Graphalytics: Graph Analytics Benchmark for Big Data Frameworks

Graphalytics is a benchmark for graph processing frameworks.

## Getting started

A packaged version of Graphalytics is currently available on the @Large server, in `/data/graphalytics/`. Packages are named `graphalytics-platforms-${platform}-${version}-bin.tar.gz`, so you can download the latest version for the platform you wish to benchmark. Currently, only MapReduce version 2 (labeled `mapreducev2`) and Giraph (`giraph`) are available.  Building from source can be done on any machine with Apache Maven 3.x (see "How to build Graphalytics?"). After unpacking the distribution or building from source, there are three steps to prepare the benchmark:

 1. Add graphs to the benchmark (see "How to add graphs to Graphalytics?").
 2. Edit the Graphalytics configuration (see "How to configure Graphalytics?").
 3. Ensure the platform under test is configured and running (see "Platform-specific information").

To run the benchmark, execute the launch script (e.g., for MapReduce v2):

```
./run-benchmark.sh mapreducev2
```

After the benchmark has completed, the results can be found in `${platform}-report`

## How to build Graphalytics?

The source of Graphalytics is available on the @Large server, as `/data/graphalytics/graphalytics-0.0.1-src.tar.xz`. Before building you need to specify a Hadoop version in the `pom.xml` file in the extracted directory. For `graphalytics-giraph` you also need to compile a patched version of Giraph (see "Building Giraph"). Once all previous steps are completed, you can use the `compile-benchmark.sh` script to build Graphalytics, e.g.:

```
./compile-benchmark.sh mapreducev2
```

to compile `graphalytics-mapreducev2`. You can find the generated distribution archive in the `target` directory.

### Building Giraph

`graphalytics-giraph` requires a patched version of Giraph 1.1.0 with "Pure YARN mode" enabled. This version is not available through Maven, and needs to be built before building `graphalytics-giraph`. To prepare for building, clone the official Giraph repository using git, checkout the `release-1.1` branch, and apply the patch that can be found in the Graphalytics source (`platforms/giraph/giraph-on-yarn.patch`). The exact Maven command for building depends on the required version of Hadoop. For versions before 2.5 use:

```
mvn -Phadoop_yarn -Dhadoop.version=2.4.0 clean package install
```

substituting `2.4.0` with the desired version. Giraph supports versions as far back as 2.0, but the provided patch has not been verified to work for versions before 2.4.0. For Hadoop 2.5.0 and later use:

```
mvn -Phadoop_yarn_2.5 -Dhadoop.version=2.5.0 clean package install
```

substituting `2.5.0` with the desired version. These maven commands will build Giraph and install it to your local maven repository, which will be used to build `graphalytics-giraph`.

## How to add graphs to Graphalytics?

Prepared graphs can be found on the @Large server in `/data/graphalytics/graphs/`. Download some or all graph files and store them locally (we will assume in `/local/graphs/` for this guide). Finally, you must edit the `graphs.root-directory` property in `config/graphs.properties` file to point to the graphs you have downloaded, e.g.:

```
graphs.root-directory = /local/graphs/
```

If you did not download all graphs, you need to specify which graphs you have by editing the `graphs.names` property. For example:

```
graphs.names = ldbc-1, ldbc-3, ldbc-10
```

to select only the three smallest LDBC graphs.

## How to configure Graphalytics?

You can specify in the Graphalytics configuration a subset of graphs and algorithms to run. By default, all algorithms are run on all the available graphs. This can be changed by creating a "run" properties file in `config/runs/`. See `config/runs/example.properties` for an example. A particular run can be selected by editing `config/benchmark.properties` and including a different file from the `runs` subdirectory.

## Platform-specific information

### MapReduce V2

The `mapreducev2` benchmark runs on Hadoop version 2.4.1 or later (may work for earlier versions, this has not been verified). Before launching the benchmark, ensure Hadoop is operational and in either pseudo-distributed or distributed mode. Next, edit the `mapreducev2`-specific configuration file: `config/mapreducev2.properties` and change the following settings:

 - `hadoop.home`: Set to the root of your Hadoop installation (HADOOP_HOME).
 - `mapreducev2.reducer-count`: Set to an appropriate number of reducers for your Hadoop deployment (note: variable number of reducers per graph/algorithm is not yet supported).

### Giraph

The `giraph` benchmark runs on Hadoop version 2.4.1 or later (earlier versions have not been attempted) and requires ZooKeeper (tested with 3.4.1). The benchmark includes a copy of Giraph version 1.1.0 compiled to run in "pure YARN" mode (i.e., without the MapReduce layer). Before launching the benchmark, ensure Hadoop is running in either pseudo-distributed or distributed mode, and ensure that the ZooKeeper service is running. Note that the Giraph benchmark will use MapReduce jobs to preprocess the input graphs, so MapReduce must be properly configured. Next, edit `config/giraph.properties` and change the following settings:

 - `hadoop.home`: Set to the root of your Hadoop installation (HADOOP_HOME).
 - `giraph.zoo-keeper-address`: Set to the hostname and port on which ZooKeeper is running.
 - `giraph.preprocessing.num-reducers`: Set to an appropriate number of reducers for the MR cluster.
 - `giraph.job.heap-size`: Set to the amount of memory (in MB) each worker should have.
 - `giraph.job.worker-count`: Set to an appropriate number of workers for the Hadoop cluster. Note that Giraph launches an Application Master (512 MB) and `worker-count + 1` containers of size `giraph.job.heap-size`.

