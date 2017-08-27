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
package science.atlarge.graphalytics.execution;

import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The setup of the benchmark run.
 * @author Wing Lung Ngai
 */
public class BenchmarkRunSetup implements Serializable {

    private Path logDir;
    private Path outputDir;
    private Path validationDir;

    private boolean outputRequired;
    private boolean validationRequired;

    public BenchmarkRunSetup(BenchmarkRun benchmarkRun,
                             Path logDir, Path outputDir, Path validationDir,
                             boolean outputRequired, boolean validationRequired) {

        this.logDir = logDir.resolve(benchmarkRun.getName());
        this.outputDir = outputDir.resolve(benchmarkRun.getName());
        this.validationDir = validationDir.resolve(benchmarkRun.getGraph().getName()
                + "-" + benchmarkRun.getAlgorithm().getAcronym());

        this.outputRequired = outputRequired;
        this.validationRequired = validationRequired;
    }


    public Path getLogDir() {
        return logDir;
    }

    /**
     * @return the path to write the output to, or the prefix if multiple output files are required
     */
    public Path getOutputDir() {
        return outputDir;
    }

    /**
     * @return the path to file containing the validation output of this benchmark.
     */
    public Path getValidationDir() {
        return validationDir;
    }

    /**
     * @return true iff the output of the algorithm will be validation by the benchmark suite.
     */
    public boolean isValidationRequired() {
        return validationRequired;
    }

    /**
     * @return true iff the output of the algorithm should be written to (a) file(s)
     */
    public boolean isOutputRequired() {
        return outputRequired;
    }

    @Override
    public String toString() {
        return String.format("output=%s, validation=%s",
                outputRequired ? "enabled" : "disabled",
                validationRequired ? "enabled" : "disabled");
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {

        stream.writeObject(logDir.toAbsolutePath().toString());
        stream.writeObject(outputDir.toAbsolutePath().toString());
        stream.writeObject(validationDir.toAbsolutePath().toString());

        stream.writeBoolean(outputRequired);
        stream.writeBoolean(validationRequired);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {

        logDir = Paths.get(((String) stream.readObject()));
        outputDir = Paths.get(((String) stream.readObject()));
        validationDir = Paths.get(((String) stream.readObject()));

        outputRequired = stream.readBoolean();
        validationRequired =  stream.readBoolean();
    }
}
