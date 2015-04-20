# Graphalytics: A big data benchmark for graph-processing platforms

[![Build Status](http://jenkins.tribler.org/buildStatus/icon?job=Graphalytics_master_tester)](http://jenkins.tribler.org/job/Graphalytics_master_tester/)

Graphalytics supports the following platforms: MapReduce version 2 (labeled `mapreducev2`), Giraph (`giraph`), GraphX (`graphx`), GraphLab Create (`graphlab`), and Neo4j (`neo4j`). 


## Getting started

To use Graphalytics, go through the following steps:

 1. Build Graphalytics (see "How to build Graphalytics?").
 2. Add graphs to the benchmark (see "How to add graphs to Graphalytics?").
 3. Edit the Graphalytics configuration (see "How to configure Graphalytics?").
 4. Ensure the platform under test is configured and running (see "Platform-specific information").
 5. Run the benchmark by executing the launch script, e.g., for MapReduce v2:

```
./run-benchmark.sh mapreducev2
```

After the benchmark has completed, the results can be found in `${platform}-report`.


## How to build Graphalytics?

Graphalytics uses Apache Maven 3. Before building, you need to specify a Hadoop version in the `pom.xml` file. Afterwards, you can use the `compile-benchmark.sh` script to build Graphalytics, e.g., to compile `graphalytics-mapreducev2` without running unit tests:

```
./compile-benchmark.sh --no-tests mapreducev2
```

You can find the generated distribution archive in the root of the repository.


## How to add graphs to Graphalytics?

You can download synthetic graphs generated with the LDBC-SNB Data Generator from:
http://atlarge.ewi.tudelft.nl/graphalytics/

You must edit the `graphs.root-directory` property in `config/graphs.properties` file to point to the graphs you have downloaded, e.g.:

```
graphs.root-directory = /local/graphs/
```

Graphalytics detects at runtime which graphs are available by checking for the existence of supported graphs in the directory specified by `graphs.root-directory`. 


## How to configure Graphalytics?

You can specify in the Graphalytics configuration a subset of graphs and algorithms to run. By default, all algorithms are run on all the available graphs. This can be changed by creating a "run" properties file in `config/runs/`. See `config/runs/example.properties` for an example. A particular run can be selected by editing `config/benchmark.properties` and including a different file from the `runs` subdirectory.

To simplify future updates to newer versions of Graphalytics, it is recommended to create a copy of the `config` directory and edit configuration files in the copied directory. Updating Graphalytics may change files in the `config` directory, but leave local changes in the copied directory untouched. The configuration folder can be specified when using the `run-benchmark.sh` script by setting the `--config` flag. An example workflow is:

```
cp -R config my-local-configuration
# Edit files in my-local-configuration
./run-benchmark --config my-local-configuration mapreducev2
```

## Platform-specific information


### MapReduce V2

The `mapreducev2` benchmark runs on Hadoop version 2.4.1 or later (may work for earlier versions, this has not been verified). Before launching the benchmark, ensure Hadoop is operational and in either pseudo-distributed or distributed mode. Next, edit the `mapreducev2`-specific configuration file: `config/mapreducev2.properties` and change the following setting:

 - `mapreducev2.reducer-count`: Set to an appropriate number of reducers for your Hadoop deployment (note: variable number of reducers per graph/algorithm is not yet supported).

Because the MapReduce benchmark uses Hadoop, you must also edit `config/hadoop.properties` to change the following setting:

 - `hadoop.home`: Set to the root of your Hadoop installation (HADOOP_HOME).

### Giraph

The `giraph` benchmark runs on Hadoop version 2.4.1 or later (earlier versions have not been attempted) and requires ZooKeeper (tested with 3.4.1). Before launching the benchmark, ensure Hadoop is running in either pseudo-distributed or distributed mode, and ensure that the ZooKeeper service is running. Next, edit `config/giraph.properties` and change the following settings:

 - `giraph.zoo-keeper-address`: Set to the hostname and port on which ZooKeeper is running.
 - `giraph.job.heap-size`: Set to the amount of heap space (in MB) each worker should have. As Giraph runs on MapReduce, this setting corresponds to the JVM heap specified for each map task, i.e., `mapreduce.map.java.opts`.
 - `giraph.job.memory-size`: Set to the amount of memory (in MB) each worker should have. This corresponds to the amount of memory requested from the YARN resource manager for each worker, i.e., `mapreduce.map.memory.mb`.
 - `giraph.job.worker-count`: Set to an appropriate number of workers for the Hadoop cluster. Note that Giraph launches an additional master process.

Because the Giraph benchmark uses Hadoop, you must also edit `config/hadoop.properties` as explained in the MapReduce section.

### GraphX

The `graphx` benchmark uses YARN version 2.4.1 or later (earlier versions have not been attempted) to deploy Spark. Before launching the benchmark, ensure Hadoop is running in either pseudo-distributed or distributed mode. Next, edit `config/graphx.properties` and change the following settings:

 - `graphx.job.num-executors`: Set to the number of Spark workers to use.
 - `graphx.job.executor-memory`: Set to the amount of memory to reserve in YARN for each worker.
 - `graphx.job.executor-cores`: Set to the number of cores available to each worker.

Because the GraphX benchmark uses Hadoop, you must also edit `config/hadoop.properties` as explained in the MapReduce section.

### GraphLab Create

The `graphlab` benchmark can run either locally or on Hadoop version 2.4.1 or later (earlier versions have not been attempted) with YARN as resource manager. Before launching the benchmark, ensure GraphLab Create is installed on your system with a valid (free) license key, the python executable is in your PATH and Hadoop is running in either pseudo-distributed or distributed mode (if used). Next, edit the `graphlab`-specific configuration file: `config/graphlab.properties` and change the following settings:

- `graphlab.target`: Set to `local` to run the benchmark on the machine on which Graphalytics is running, or `hadoop` to deploy GraphLab to a Hadoop cluster.
- `graphlab.job.virtual-cores`: Set to the amount of virtual cores to request from your Hadoop deployment.
- `graphlab.job.heap-size`: Set to the amount of heap space (in MB) is required for job execution in the Hadoop environment.

If Hadoop is used for the benchmark, you must also edit `config/hadoop.properties` as explained in the MapReduce section.

### Neo4j

The `neo4j` benchmark is run locally on the machine on which Graphalytics is launched. Before launching the benchmark, edit `config/neo4j.properties` and change the following settings:

- `jvm.heap.size.mb`: Set to the amount of heap space (in MB) to allocate to the Neo4j process.
- `neo4j.db.path`: Set to an appropriate path to store Neo4j databases in.

Other options in the `config/neo4j.properties` file are passed directly to Neo4j. This should be used to set e.g. buffer sizes.
