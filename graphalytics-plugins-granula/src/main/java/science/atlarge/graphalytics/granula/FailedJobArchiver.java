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
package science.atlarge.graphalytics.granula;

import science.atlarge.granula.archiver.GranulaExecutor;
import science.atlarge.granula.modeller.entity.Execution;
import science.atlarge.granula.modeller.job.JobModel;
import science.atlarge.granula.util.FileUtil;
import science.atlarge.granula.util.json.JsonUtil;

import java.nio.file.Paths;

/**
 * @author Wing Lung Ngai
 */
public class FailedJobArchiver {

    public static void main(String[] args) {
        String driverLogPath = args[0];
        Execution execution = (Execution) JsonUtil.fromJson(FileUtil.readFile(Paths.get(driverLogPath)), Execution.class);
        execution.setEndTime(System.currentTimeMillis());
        execution.setArcPath(Paths.get("./iffailed").toAbsolutePath().toString());
        JobModel jobModel = new JobModel(GranulaPlugin.getPlatformModelByMagic(execution.getPlatform()));

        GranulaExecutor granulaExecutor = new GranulaExecutor();
        if(execution.getPlatform().toLowerCase().contains("graphx")) {
            granulaExecutor.setEnvEnabled(false); //TODO fix graphx
        }
        granulaExecutor.setExecution(execution);
        granulaExecutor.buildJobArchive(jobModel);

    }
}
