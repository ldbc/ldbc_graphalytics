package nl.tudelft.graphalytics.granula;

import nl.tudelft.granula.archiver.GranulaExecutor;
import nl.tudelft.granula.modeller.entity.Execution;
import nl.tudelft.granula.modeller.job.JobModel;
import nl.tudelft.granula.util.FileUtil;
import nl.tudelft.granula.util.json.JsonUtil;

import java.io.IOException;
import java.nio.file.Paths;

public class FailedJobArchiver {

    public static void main(String[] args) {
        String driverLogPath = args[0];
        Execution execution = (Execution) JsonUtil.fromJson(FileUtil.readFile(Paths.get(driverLogPath)), Execution.class);
        execution.setEndTime(System.currentTimeMillis());
        execution.setArcPath(Paths.get("./iffailed").toAbsolutePath().toString());
        JobModel jobModel = new JobModel(GranulaPlugin.getPlatformModel(execution.getPlatform()));

        GranulaExecutor granulaExecutor = new GranulaExecutor();
        granulaExecutor.setExecution(execution);
        granulaExecutor.buildJobArchive(jobModel);

    }
}
