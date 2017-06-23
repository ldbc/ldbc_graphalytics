package science.atlarge.graphalytics.granula;

import science.atlarge.granula.archiver.GranulaExecutor;
import science.atlarge.granula.modeller.entity.Execution;
import science.atlarge.granula.modeller.job.JobModel;
import science.atlarge.granula.util.FileUtil;
import science.atlarge.granula.util.json.JsonUtil;

import java.nio.file.Paths;

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
