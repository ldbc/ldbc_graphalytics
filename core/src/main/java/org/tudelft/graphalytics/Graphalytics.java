package org.tudelft.graphalytics;

import java.util.ArrayList;
import java.util.Set;

import org.reflections.Reflections;

public class Graphalytics {

	public static void main(String[] args) {
		// Get the first command-line argument (platform name)
		if (args.length < 1) {
			System.err.println("Missing argument <platform>.");
			System.exit(1);
		}
		String platform = args[0];
		
		// Use the Reflections library to find the Platform subclass for the given platform
		Reflections reflections = new Reflections("org.tudelft.graphalytics." + platform);
		Set<Class<? extends Platform>> platformClasses = reflections.getSubTypesOf(Platform.class);
		if (platformClasses.size() == 0) {
			System.err.println("Cannot find a subclass of \"org.tudelft.graphalytics.Platform\" in package \"" +
					"org.tudelft.graphalytics." + platform + "\".");
			System.exit(2);
		} else if (platformClasses.size() > 1) {
			System.err.println("Found multiple subclasses of \"org.tudelft.graphalytics.Platform\"" +
					"in package \"org.tudelft.graphalytics." + platform + "\".");
			System.exit(3);
		}
		
		// Attempt to instantiate the Platform subclass to run the benchmark
		Platform platformInstance = null;
		try {
			platformInstance = new ArrayList<>(platformClasses).get(0).newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			System.exit(4);
		}
		
		BenchmarkSuite benchmark = new BenchmarkSuite("/data/tudelft/graphalytics-graphs/");
		try {
			benchmark.runOnPlatform(platformInstance);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(5);
		}
	}

}
