package org.tudelft.graphalytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.tudelft.graphalytics.reporting.BenchmarkReport;
import org.tudelft.graphalytics.reporting.Graph;
import org.tudelft.graphalytics.reporting.Algorithm;
import org.tudelft.graphalytics.reporting.Result;

public class Graphalytics {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		// Get the first command-line argument (platform name)
		if (args.length < 1) {
			log.fatal("Missing argument <platform>.");
			System.exit(1);
		}
		String platform = args[0];
		
		// Use the Reflections library to find the Platform subclass for the given platform
		Reflections reflections = new Reflections("org.tudelft.graphalytics." + platform);
		Set<Class<? extends Platform>> platformClasses = reflections.getSubTypesOf(Platform.class);
		if (platformClasses.size() == 0) {
			log.fatal("Cannot find a subclass of \"org.tudelft.graphalytics.Platform\" in package \"" +
					"org.tudelft.graphalytics." + platform + "\".");
			System.exit(2);
		} else if (platformClasses.size() > 1) {
			log.fatal("Found multiple subclasses of \"org.tudelft.graphalytics.Platform\"" +
					"in package \"org.tudelft.graphalytics." + platform + "\".");
			System.exit(3);
		}
		
		// Attempt to instantiate the Platform subclass to run the benchmark
		Platform platformInstance = null;
		try {
			platformInstance = new ArrayList<>(platformClasses).get(0).newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			log.catching(Level.FATAL, e);
			System.exit(4);
		}
		
		
		
		// Test benchmark report
//		Map<String, Graph> graphs = new HashMap<>();
//		Map<String, Algorithm> algorithms = new HashMap<>();
//		graphs.put("G1", new Graph("G1"));
//		graphs.put("G2", new Graph("G2"));
//		graphs.put("G3", new Graph("G3"));
//		algorithms.put("A1", new Algorithm("A1"));
//		algorithms.put("A2", new Algorithm("A2"));
//		Result A1G1 = new Result(algorithms.get("A1"), graphs.get("G1"));
//		Result A1G2 = new Result(algorithms.get("A1"), graphs.get("G2"));
//		Result A2G2 = new Result(algorithms.get("A2"), graphs.get("G2"));
//		Result A2G3 = new Result(algorithms.get("A2"), graphs.get("G3"));
//		graphs.get("G1").addResult("A1", A1G1);
//		graphs.get("G2").addResult("A1", A1G2);
//		graphs.get("G2").addResult("A2", A2G2);
//		graphs.get("G3").addResult("A2", A2G3);
//		algorithms.get("A1").addResult("G1", A1G1);
//		algorithms.get("A1").addResult("G2", A1G2);
//		algorithms.get("A2").addResult("G2", A2G2);
//		algorithms.get("A2").addResult("G3", A2G3);
//		A1G1.setRuntimeMs(1500); A1G1.setSucceeded(true);
//		A1G2.setRuntimeMs(10500); A1G2.setSucceeded(true);
//		A2G2.setSucceeded(false);
//		A2G3.setRuntimeMs(1234567); A2G3.setSucceeded(true);
//		BenchmarkReport report = BenchmarkReport.fromTestData(graphs, algorithms);
//		try {
//			report.generate("report-template/", "sample-report/");
//		} catch (IOException ex) {
//			log.catching(ex);
//		}
//		System.exit(0);
		
		
		
		// Run the benchmark
		BenchmarkSuite benchmark = BenchmarkSuite.readFromProperties();
		if (benchmark == null) {
			System.exit(5);
		}
		try {
			benchmark.runOnPlatform(platformInstance);
		} catch (Exception e) {
			log.catching(Level.FATAL, e);
			System.exit(6);
		}
	}

}
