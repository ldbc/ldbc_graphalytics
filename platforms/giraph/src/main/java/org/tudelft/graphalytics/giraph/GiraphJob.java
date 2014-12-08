package org.tudelft.graphalytics.giraph;

import java.io.File;

import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.conf.IntConfOption;
import org.apache.giraph.conf.StrConfOption;
import org.apache.giraph.graph.Computation;
import org.apache.giraph.io.EdgeInputFormat;
import org.apache.giraph.io.EdgeOutputFormat;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexOutputFormat;
import org.apache.giraph.io.formats.GiraphFileInputFormat;
import org.apache.giraph.yarn.GiraphYarnClient;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for all jobs in the Giraph benchmark suite. Configures and executes
 * a Giraph job using the computation and vertex format specified by subclasses
 * of GiraphJob. In addition, a pre-execution hook is provided to enable arbitrary
 * job-specific configuration to be added to the job.
 * 
 * @author Tim Hegeman
 */
public abstract class GiraphJob extends Configured implements Tool {
	private static final Logger LOG = LogManager.getLogger();
	
	/** The configuration key for the Giraph worker heap size in megabytes. */
	public static final String HEAP_SIZE_MB_KEY = "graphalytics.giraphjob.heap-size-mb";
	/** The Giraph worker heap size in megabytes. */
	public static final IntConfOption HEAP_SIZE_MB = new IntConfOption(HEAP_SIZE_MB_KEY,
			1024, "Giraph worker heap size in megabytes");
	
	/** The configuration key for the number of Giraph workers to used. */
	public static final String WORKER_COUNT_KEY = "graphalytics.giraphjob.worker-count";
	/** The number of Giraph workers to be used. */
	public static final IntConfOption WORKER_COUNT = new IntConfOption(WORKER_COUNT_KEY,
			1, "Number of Giraph workers to use");
	
	/** The configuration key for the input path of the Giraph job. */
	public static final String INPUT_PATH_KEY = "graphalytics.giraphjob.input-path";
	/** The input path of the Giraph job. */
	public static final StrConfOption INPUT_PATH = new StrConfOption(INPUT_PATH_KEY,
			"", "Giraph input path");
	
	/** The configuration key for the output path of the Giraph job. */
	public static final String OUTPUT_PATH_KEY = "graphalytics.giraphjob.output-path";
	/** The output path of the Giraph job. */
	public static final StrConfOption OUTPUT_PATH = new StrConfOption(OUTPUT_PATH_KEY,
			"", "Giraph output path");
	
	/** The configuration key for the ZooKeeper address used by Giraph. */
	public static final String ZOOKEEPER_ADDRESS_KEY = "graphalytics.giraphjob.zookeeper-address";
	/** The ZooKeeper address used by Giraph (hostname:port). */
	public static final StrConfOption ZOOKEEPER_ADDRESS = new StrConfOption(ZOOKEEPER_ADDRESS_KEY,
			"", "ZooKeeper address");

	private String inputPath;
	private String outputPath;
	private String zooKeeperAddress;
	private int workerCount;
	private int heapSize;

	private void loadConfiguration() {
		if (INPUT_PATH.isDefaultValue(getConf()))
			throw new IllegalStateException("Missing mandatory configuration: " + INPUT_PATH_KEY);
		if (OUTPUT_PATH.isDefaultValue(getConf()))
			throw new IllegalStateException("Missing mandatory configuration: " + OUTPUT_PATH_KEY);
		if (ZOOKEEPER_ADDRESS.isDefaultValue(getConf()))
			throw new IllegalStateException("Missing mandatory configuration: " + ZOOKEEPER_ADDRESS_KEY);
		
		workerCount = WORKER_COUNT.get(getConf());
		heapSize = HEAP_SIZE_MB.get(getConf());
		inputPath = INPUT_PATH.get(getConf());
		outputPath = OUTPUT_PATH.get(getConf());
		zooKeeperAddress = ZOOKEEPER_ADDRESS.get(getConf());
	}

	/**
	 * Creates a new Giraph job configuration and loads it with generic options
	 * such as input and output paths, the number of workers, and the worker
	 * heap size. It sets the computation and I/O format classes based on
	 * the return value of their respective hooks. The configure method is called
	 * to allow for job-specific configuration. Finally, the Giraph job is
	 * submitted and is executed (blocking).
	 * 
	 * @param args ignored
	 * @return zero iff the job completed successfully
	 */
	@Override
	public final int run(String[] args) throws Exception {
		loadConfiguration();
		
		// Prepare the job configuration
		GiraphConfiguration configuration = new GiraphConfiguration();
		
		// Set the input and output path
		GiraphFileInputFormat.addVertexInputPath(configuration, new Path(inputPath));
		configuration.set(FileOutputFormat.OUTDIR, outputPath);
		
		// Set the job-specific classes
		configuration.setComputationClass(getComputationClass());
		configuration.setVertexInputFormatClass(getVertexInputFormatClass());
		configuration.setVertexOutputFormatClass(getVertexOutputFormatClass());
		configuration.setEdgeInputFormatClass(getEdgeInputFormatClass());
		configuration.setEdgeOutputFormatClass(getEdgeOutputFormatClass());
		
		// Set deployment-specific configuration from external configuration files
		configuration.setWorkerConfiguration(workerCount, workerCount, 100.0f);
		configuration.setZooKeeperConfiguration(zooKeeperAddress);
		configuration.setYarnTaskHeapMb(heapSize);
		
		// Set algorithm-specific configuration
		configure(configuration);
		
		// Ensure the benchmark JAR is loaded
		String jarName = new File(
					getClass().getProtectionDomain().getCodeSource().getLocation().getPath()
				).getName();
		configuration.setYarnLibJars(jarName);
		
		// Launch the YARN client
		GiraphYarnClient yarnClient = new GiraphYarnClient(configuration, "Graphalytics: " +
				getClass().getSimpleName());
		LOG.debug("Running Giraph job on YARN...");
		return LOG.exit(yarnClient.run(true) ? 0 : -1);
	}
	
	/**
	 * Hook for subclasses of GiraphJob to specify which Computation to run for
	 * the Giraph job.
	 * 
	 * @return a job-specific Computation class
	 */
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends Computation> getComputationClass();
	
	/**
	 * Hook for subclasses of GiraphJob to specify which input format to use
	 * when importing vertices into Giraph.
	 * 
	 * @return a job-specific VertexInputFormat
	 */
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends VertexInputFormat> getVertexInputFormatClass();
	
	/**
	 * Hook for subclasses of GiraphJob to specify which output format to use
	 * when storing the resulting vertices of a Giraph computation.
	 * 
	 * @return a job-specific VertexOutputFormat
	 */
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends VertexOutputFormat> getVertexOutputFormatClass();
	
	/**
	 * Hook for subclasses of GiraphJob to specify which input format to use
	 * when importing edges into Giraph.
	 * 
	 * @return a job-specific VertexInputFormat
	 */
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends EdgeInputFormat> getEdgeInputFormatClass();
	
	/**
	 * Hook for subclasses of GiraphJob to specify which output format to use
	 * when storing the resulting edges of a Giraph computation.
	 * 
	 * @return a job-specific VertexOutputFormat
	 */
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends EdgeOutputFormat> getEdgeOutputFormatClass();
	
	/**
	 * Hook for subclasses of GiraphJob to set arbitrary configuration for the
	 * Giraph job. Often used to set global parameters needed for a specific
	 * graph processing algorithm.
	 * 
	 * @param config the Giraph configuration for this job
	 */
	protected abstract void configure(GiraphConfiguration config);
	
}
