package org.tudelft.graphalytics.giraph;

import java.io.File;

import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.Computation;
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

public abstract class GiraphJob extends Configured implements Tool {
	private static final Logger LOG = LogManager.getLogger();

	private String inputPath;
	private String outputPath;
	private String zooKeeperAddress;
	private int workerCount = 1;
	private int heapSize = 1024;
	
	public GiraphJob(String inputPath, String outputPath, String zooKeeperAddress) {
		this.inputPath = inputPath;
		this.outputPath = outputPath;
		this.zooKeeperAddress = zooKeeperAddress;
	}
	
	public void setWorkerCount(int workerCount) {
		this.workerCount = workerCount;
	}
	public void setHeapSize(int heapSize) {
		this.heapSize = heapSize;
	}
	
	protected String getOutputPath() {
		return outputPath;
	}
	
	@Override
	public int run(String[] args) throws Exception {
		// Prepare the job configuration
		GiraphConfiguration configuration = new GiraphConfiguration();
		
		// Set the input and output path
		GiraphFileInputFormat.addVertexInputPath(configuration, new Path(inputPath));
		configuration.set(FileOutputFormat.OUTDIR, outputPath);
		
		// Set the job-specific classes
		configuration.setComputationClass(getComputationClass());
		configuration.setVertexInputFormatClass(getVertexInputFormatClass());
		configuration.setVertexOutputFormatClass(getVertexOutputFormatClass());
		
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
	
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends Computation> getComputationClass();
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends VertexInputFormat> getVertexInputFormatClass();
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends VertexOutputFormat> getVertexOutputFormatClass();
	
	protected abstract void configure(GiraphConfiguration config);
	
}
