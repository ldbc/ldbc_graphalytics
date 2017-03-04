#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.graphalytics.${platform-acronym};

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ${package}.graphalytics.validation.GraphStructure;

public class Utils {
	private static final Logger LOG = LogManager.getLogger();

	public static void writeVerticesToFile(GraphStructure graph, File f) throws IOException {
		BufferedWriter w = new BufferedWriter(new FileWriter(f));
		
		for (long v: graph.getVertices()) {
			w.write(String.format("%d${symbol_escape}n", v));
		}
		
		w.flush();
		w.close();
	}
	
	public static void writeEdgeToFile(GraphStructure graph, boolean directed, File f) throws IOException {
		BufferedWriter w = new BufferedWriter(new FileWriter(f));
		
		for (long v: graph.getVertices()) {
			for (long u: graph.getEdgesForVertex(v)) {
				if (directed || v < u) {
					w.write(String.format("%d %d${symbol_escape}n", v, u));
				}
			}
		}
		
		w.flush();
		w.close();
	}
	
	public static <T> Map<Long, T> readResults(File f, Class<T> clazz) throws Exception {
		Map<Long, T> results = new HashMap<Long, T>();
		
		BufferedReader r = new BufferedReader(new FileReader(f));
		String line;
		
		while ((line = r.readLine()) != null) {
			String tokens[] = line.split(" ", 2);
			
			Long vertex = Long.valueOf(tokens[0]);
			T value = clazz.getDeclaredConstructor(String.class).newInstance(tokens[1]);
			results.put(vertex, value);
		}
		
		return results;
	}
	
	public static Configuration loadConfiguration() {
		try {
			return new PropertiesConfiguration(${platform-name}Platform.PLATFORM_PROPERTIES_FILE);
		} catch(ConfigurationException e) {
			LOG.warn("failed to load " + ${platform-name}Platform.PLATFORM_PROPERTIES_FILE, e);
			return new PropertiesConfiguration();
		}
	}
}
