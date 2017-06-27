/*
 * Copyright 2015 - 2017 Atlarge Research Team,
 * operating at Technische Universiteit Delft
 * and Vrije Universiteit Amsterdam, the Netherlands.
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
package science.atlarge.graphalytics.validation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Wing Lung Ngai
 */
public class VertexCounter {

    private static final Logger LOG = LogManager.getLogger(VertexCounter.class);

    final private Path outputPath;

    public VertexCounter(Path outputPath) {
        this.outputPath = outputPath;
    }

    public long count() throws ValidatorException {

        try {
            long totalNumVertices = 0;
            final List<Long> numVerticesPerFile = new ArrayList<>();

            // report vertex count every file.
            Files.walkFileTree(outputPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    long numVertices = 0;
                    // count the number of non-empty lines
                    try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (line.isEmpty()) {
                                continue;
                            }
                            numVertices++;
                        }
                        numVerticesPerFile.add(numVertices);
                        LOG.warn(String.format("Counted %s lines of outputs at %s.", numVertices, file));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            // count total number of vertices.
            for (Long numVertices : numVerticesPerFile) {
                totalNumVertices += numVertices;
            }

            return totalNumVertices;
        } catch (IOException e) {
            throw new ValidatorException("Failed to read output file/directory '" + outputPath + "'");
        }
    }
}
