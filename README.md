# Graphalytics: Graph Analytics Benchmark for Big Data Platforms

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

Graphalytics uses Apache Maven 3. Before building, you need to specify a Hadoop version in the `pom.xml` file. Afterwards, you can use the `compile-benchmark.sh` script to build Graphalytics, e.g., to compile `graphalytics-mapreducev2` without running the tests:

```
./compile-benchmark.sh --no-tests mapreducev2
```

You can find the generated distribution archive in the `target` directory.


## How to add graphs to Graphalytics?

You can download synthetic graphs generated with the LDBC-SNB Data Generator from:
http://atlarge.ewi.tudelft.nl/graphalytics/

You must edit the `graphs.root-directory` property in `config/graphs.properties` file to point to the graphs you have downloaded, e.g.:

```
graphs.root-directory = /local/graphs/
```

You must specify which graphs you have by editing the `graphs.names` property. For example, to select only the three smallest LDBC graphs:

```
graphs.names = ldbc-1, ldbc-3, ldbc-10
```


## How to configure Graphalytics?

You can specify in the Graphalytics configuration a subset of graphs and algorithms to run. By default, all algorithms are run on all the available graphs. This can be changed by creating a "run" properties file in `config/runs/`. See `config/runs/example.properties` for an example. A particular run can be selected by editing `config/benchmark.properties` and including a different file from the `runs` subdirectory.


## Platform-specific information


### MapReduce V2

The `mapreducev2` benchmark runs on Hadoop version 2.4.1 or later (may work for earlier versions, this has not been verified). Before launching the benchmark, ensure Hadoop is operational and in either pseudo-distributed or distributed mode. Next, edit the `mapreducev2`-specific configuration file: `config/mapreducev2.properties` and change the following settings:

 - `hadoop.home`: Set to the root of your Hadoop installation (HADOOP_HOME).
 - `mapreducev2.reducer-count`: Set to an appropriate number of reducers for your Hadoop deployment (note: variable number of reducers per graph/algorithm is not yet supported).


### Giraph

The `giraph` benchmark runs on Hadoop version 2.4.1 or later (earlier versions have not been attempted) and requires ZooKeeper (tested with 3.4.1). Before launching the benchmark, ensure Hadoop is running in either pseudo-distributed or distributed mode, and ensure that the ZooKeeper service is running. Next, edit `config/giraph.properties` and change the following settings:

 - `hadoop.home`: Set to the root of your Hadoop installation (HADOOP_HOME).
 - `giraph.zoo-keeper-address`: Set to the hostname and port on which ZooKeeper is running.
 - `giraph.job.heap-size`: Set to the amount of heap space (in MB) each worker should have. As Giraph runs on MapReduce, this setting corresponds to the JVM heap specified for each map task, i.e., `mapreduce.map.java.opts`.
 - `giraph.job.memory-size`: Set to the amount of memory (in MB) each worker should have. This corresponds to the amount of memory requested from the YARN resource manager for each worker, i.e., `mapreduce.map.memory.mb`.
 - `giraph.job.worker-count`: Set to an appropriate number of workers for the Hadoop cluster. Note that Giraph launches an additional master process.


### GraphX

The `graphx` benchmark uses YARN version 2.4.1 or later (earlier versions have not been attempted) to deploy Spark. Before launching the benchmark, ensure Hadoop is running in either pseudo-distributed or distributed mode, and ensure that the ZooKeeper service is running. Next, edit `config/graphx.properties` and change the following settings:

 - `hadoop.home`: Set to the root of your Hadoop installation (HADOOP_HOME).
 - `graphx.job.num-executors`: Set to the number of Spark workers to use.
 - `graphx.job.executor-memory`: Set to the amount of memory to reserve in YARN for each worker.
 - `graphx.job.executor-cores`: Set to the number of cores available to each worker.


### GraphLab Create

The `graphlab` benchmark runs on Hadoop version 2.4.1 or later (earlier versions have not been attempted) with YARN as resource manager. Before launching the benchmark, ensure GraphLab Create is installed on your system with a valid (free) license key, the python executable is in your PATH and Hadoop is running in either pseudo-distributed or distributed mode. Next, edit the `graphlab`-specifig configuration file: `config/graphlab.properties` and change the following settings:

- `hadoop.home`: Set to the root of your Hadoop installation (HADOOP_HOME).
- `graphlab.job.virtual-cores`: Set to the amount of virtual cores to request from your Hadoop deployment.
- `graphlab.job.heap-size`: Set to the amount of heap space (in MB) is required for job execution in the Hadoop environment.
