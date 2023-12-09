<h1 align="center">
    Graphalytics
</h1>
<p align="center">
    A Big Data Benchmark For Graph-Processing Platforms
</p>

Graph processing is of increasing interest for many scientific areas and revenue-generating applications, such as social networking, bioinformatics, online retail, and online gaming. To address the growing diversity of graph datasets and graph-processing algorithms, developers and system integrators have created a large variety of graph-processing platforms, which we define as the combined hardware, software, and programming system that is being used to complete a graph processing task. **LDBC Graphalytics**, an industrial-grade benchmark under [LDBC](https://ldbcouncil.org), is developed to enable objective comparisons between graph processing platforms by using six representative graph algorithms, and a large variety of real-world and synthetic datasets. Visit [our website](https://ldbcouncil.org/benchmarks/graphalytics/) for the most recent updates of the Graphalytics project.

### Publication

Want to know more about Graphalytics? Read [our VLDB paper](https://www.vldb.org/pvldb/vol9/p1317-iosup.pdf) and the [specification](https://github.com/ldbc/ldbc_graphalytics_docs).

### Build & run your first benchmark

The Graphalytics provides platform drivers for the state-of-the-arts graph processing platforms. To start your first benchmark with Graphalytics, we recommend using our reference implementations:
[GraphBLAS](https://github.com/ldbc/ldbc_graphalytics_platforms_graphblas) and
[Umbra](https://github.com/ldbc/ldbc_graphalytics_platforms_umbra).
Our datasets are hosted publicly – see the [Graphalytics website](https://ldbcouncil.org/benchmarks/graphalytics/) for download instructions.

### Participate in competitions

LDBC Graphalytics hosts competitions for graph processing platforms. Are you interested in the state-of-the-art performance? To participate, reach out to Gabor Szarnyas and David Puroja. Our email addresses are under `firstname.lastname@ldbcouncil.org`.

### Building the project

The project uses the [Build Number Maven plug-in](https://www.mojohaus.org/buildnumber-maven-plugin/) to ensure reproducibility. Hence, builds fail if the local Git repository contains uncommitted changes.

To build & install locally regardless (for testing), run:

```bash
scripts/install-local.sh
```

### Deploying Maven artifacts

We use a manual process for deploying Maven artifacts for the Graphalytics framework.

1. Clone the [`graphalytics-mvn` repository](https://github.com/ldbc/graphalytics-mvn) next to the driver repository's directory.

2. In the driver repository, run:

    ```bash
    scripts/package-mvn-artifacts.sh
    ```

3. Go to the `graphalytics-mvn` directory, check whether the JAR files are correct.

4. Add the newly created and updates files using git, then commit and push.

5. Wait for approx. 5 minutes for the deployment process to finish.
