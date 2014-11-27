package org.tudelft.graphalytics.giraph;

import java.io.File;

import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexOutputFormat;
import org.apache.giraph.io.formats.GiraphFileInputFormat;
import org.apache.giraph.yarn.GiraphYarnClient;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;

public abstract class GiraphJob extends Configured implements Tool {

	private String inputPath;
	private String outputPath;
	
	public GiraphJob(String inputPath, String outputPath) {
		this.inputPath = inputPath;
		this.outputPath = outputPath;
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
		
		// TODO: Set deployment-specific configuration from external configuration files
		configuration.setWorkerConfiguration(49, 49, 100.0f);
		configuration.setZooKeeperConfiguration("node305:2181");
		configuration.setYarnTaskHeapMb(4096);
		
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
		return yarnClient.run(true) ? 0 : -1;
	}
	
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends BasicComputation> getComputationClass();
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends VertexInputFormat> getVertexInputFormatClass();
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends VertexOutputFormat> getVertexOutputFormatClass();
	
	protected abstract void configure(GiraphConfiguration config);
	
}
