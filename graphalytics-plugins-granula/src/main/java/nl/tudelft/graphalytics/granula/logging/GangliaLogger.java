/*
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics.granula.logging;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Created by wlngai on 30-10-15.
 */
public class GangliaLogger extends UtilizationLogger {

	protected static final Logger LOG = LogManager.getLogger();
	private static final String GANGLIA_DATABASE_PATH = "benchmark.run.ganglia.database-path";

	@Override
	public void collectUtilData(List<String> nodes, List<String> metrics, long startTime, long endTime, Path logDataPath) {
		//TODO need a more applicable implementation that fetch xml data based on a start time and an end time.
		PropertiesConfiguration granulaConfig;
		try {
			granulaConfig = new PropertiesConfiguration("granula.properties");
			String databasePath = granulaConfig.getString(GANGLIA_DATABASE_PATH);

			copyFolder(new File(databasePath), new File(logDataPath + "/UtilizationLog/"));

		} catch (ConfigurationException e) {
			LOG.info("Could not find or load granula.properties.");
		} catch (IOException e) {
			LOG.info("Copying utilization logs failed");
		}
	}


	private static void copyFolder(File sourceFolder, File destinationFolder) throws IOException {
		if (sourceFolder.isDirectory()) {
			if (!destinationFolder.exists()) {
				destinationFolder.mkdir();
			}

			//Get all files from source directory
			String files[] = sourceFolder.list();

			//Iterate over all files and copy them to destinationFolder one by one
			for (String file : files) {
				File srcFile = new File(sourceFolder, file);
				File destFile = new File(destinationFolder, file);

				//Recursive function call
				copyFolder(srcFile, destFile);
			}
		} else {
			Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
