package nl.tudelft.graphalytics.configuration;

import nl.tudelft.graphalytics.BenchmarkRunner;
import nl.tudelft.graphalytics.GraphalyticsLoaderException;
import nl.tudelft.graphalytics.Platform;

import java.io.InputStream;
import java.util.Scanner;

public class PlatformParser {

    public static Platform loadPlatformFromCommandLineArgs(String[] args) {
        String platformName = getPlatformName(args);
        String platformClassName = getPlatformClassName(platformName);
        Class<? extends Platform> platformClass = getPlatformClassForName(platformClassName);
        return instantiatePlatformClass(platformClass);
    }

    private static String getPlatformName(String[] args) {
        // Get the first command-line argument (platform name)
        if (args.length < 1) {
            throw new GraphalyticsLoaderException("Missing argument <platform>.");
        }
        return args[0];
    }

    private static String getPlatformClassName(String platformName) {
        InputStream platformFileStream = openPlatformFileStream(platformName);
        String platformClassName;
        try (Scanner platformScanner = new Scanner(platformFileStream)) {
            if (!platformScanner.hasNext()) {
                throw new GraphalyticsLoaderException("Expected a single line with a class name in \"" + platformName +
                        ".platform\", got an empty file.");
            }

            platformClassName = platformScanner.next();

            if (platformScanner.hasNext()) {
                throw new GraphalyticsLoaderException("Expected a single line with a class name in \"" + platformName +
                        ".platform\", got multiple words.");
            }
        }
        return platformClassName;
    }

    private static InputStream openPlatformFileStream(String platformName) {
        // Read the <platform>.platform file that should be in the classpath to determine which class to load
        InputStream platformFileStream = BenchmarkRunner.class.getResourceAsStream("/" + platformName + ".platform");
        if (platformFileStream == null) {
            throw new GraphalyticsLoaderException("Missing resource \"" + platformName + ".platform\".");
        }
        return platformFileStream;
    }

    private static Class<? extends Platform> getPlatformClassForName(String platformClassName) {
        // Load the class by name
        Class<? extends Platform> platformClass;
        try {
            Class<?> platformClassUncasted = Class.forName(platformClassName);
            if (!Platform.class.isAssignableFrom(platformClassUncasted)) {
                throw new GraphalyticsLoaderException("Expected class \"" + platformClassName +
                        "\" to be a subclass of \"nl.tudelft.graphalytics.Platform\".");
            }

            platformClass = platformClassUncasted.asSubclass(Platform.class);
        } catch (ClassNotFoundException e) {
            throw new GraphalyticsLoaderException("Could not find class \"" + platformClassName + "\".", e);
        }
        return platformClass;
    }

    private static Platform instantiatePlatformClass(Class<? extends Platform> platformClass) {
        Platform platformInstance;
        try {
            platformInstance = platformClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new GraphalyticsLoaderException("Failed to instantiate platform class \"" +
                    platformClass.getSimpleName() + "\".", e);
        }
        return platformInstance;
    }
}
