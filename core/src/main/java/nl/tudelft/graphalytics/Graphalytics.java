package nl.tudelft.graphalytics;

import java.util.ArrayList;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

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
		Reflections reflections = new Reflections("nl.tudelft.graphalytics." + platform);
		Set<Class<? extends Platform>> platformClasses = reflections.getSubTypesOf(Platform.class);
		if (platformClasses.size() == 0) {
			log.fatal("Cannot find a subclass of \"nl.tudelft.graphalytics.Platform\" in package \"" +
					"nl.tudelft.graphalytics." + platform + "\".");
			System.exit(2);
		} else if (platformClasses.size() > 1) {
			log.fatal("Found multiple subclasses of \"nl.tudelft.graphalytics.Platform\"" +
					"in package \"nl.tudelft.graphalytics." + platform + "\".");
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
