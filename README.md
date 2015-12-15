# Graphalytics: A big data benchmark for graph-processing platforms

[![Build Status](http://jenkins.tribler.org/buildStatus/icon?job=Graphalytics_master_tester)](http://jenkins.tribler.org/job/Graphalytics_master_tester/)


## Getting started

To use Graphalytics, go through the following steps:

 1. Build Graphalytics (see "How to build Graphalytics?").
 2. Edit the Graphalytics configuration (see "How to configure Graphalytics?").
 3. Add graphs to the benchmark (see "How to add graphs to Graphalytics?").
 4. Ensure the platform under test is configured and running (see documentation of the Graphalytics platform extension).
 5. Run the benchmark by executing the launch script, `run-benchmark.sh`.

After the benchmark has completed, the results can be found in `${platform}-report-${timestamp}`.


## How to build Graphalytics?

The Graphalytics benchmark suite consists of several Git repositories: the core repository (`graphalytics`) and one additional repository per supported graph-processing platform (`graphalytics-platform-${name}`). To build Graphalytics, you must clone the core repository and the platform repositories of any platforms you intend to benchmark.

 1. Run `mvn install` once in the core repository to install the Graphalytics core to a local Maven repository.
 2. Run `mvn package` in the platform repository to create a binary of the platform extension and a distributable archive ("Graphalytics distribution").

After building the benchmark, the created archive is available in the root of the platform repository. This archive should be extracted on the machine controlling the benchmark process. Configuring and executing the benchmark will be done in the extracted directory.


## How to configure Graphalytics?

The Graphalytics distribution includes a `config-template` directory containing (template) configuration files for the Graphalytics core and the platform extension. Before editing any configuration files, it is recommended to create a copy of the `config-template` directory and name it `config`.

You can specify in the Graphalytics configuration a subset of graphs and algorithms to run. By default, all algorithms are run on all the available graphs. This can be changed by creating a "run" properties file in `config/runs/`. See `config/runs/example.properties` for an example. A particular run can be selected by editing `config/benchmark.properties` and including a different file from the `runs` subdirectory.


## How to add graphs to Graphalytics?

You can download supported graphs (including synthetic graphs generated with the LDBC-SNB Data Generator, and a variety of real world graphs) from:
[http://atlarge.ewi.tudelft.nl/graphalytics/](http://atlarge.ewi.tudelft.nl/graphalytics/).
Note that the provided graphs need to be decompressed before running the benchmark.

You must edit the `graphs.root-directory` property in `config/graphs.properties` file to point to the graphs you have downloaded, e.g.:

```
graphs.root-directory = /local/graphs/
```

Graphalytics detects at runtime which graphs are available by checking for the existence of supported graphs in the directory specified by `graphs.root-directory`. 

