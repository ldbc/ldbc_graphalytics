package org.testsuite.job.utils;

import org.apache.log4j.Logger;

import java.util.List;

public class BenchmarkPrinter {
    static Logger log = Logger.getLogger(BenchmarkPrinter.class.getName());

    public static void printBenchmarkData(JobExecutionStats jobExeStat){
        log.info("****** Job Benchmark Data ******");
        if(jobExeStat.getTime().size() > 1) {
            StringBuilder timeStr = new StringBuilder();
            timeStr.append("elapsed time =");
            for(Double time : jobExeStat.getTime())
                timeStr.append(" | "+time);
            log.info(timeStr.toString());
            log.info("AVG elapsed time = "+BenchmarkPrinter.printAVGTime(jobExeStat.getTime()));
        } else
            log.info("elapsed time = "+jobExeStat.getTime().get(0));
        log.info("node size = "+jobExeStat.getNodeSize());
        log.info("edge size = "+jobExeStat.getEdgeSize());
        log.info("****** Job Benchmark Data END ******");
    }

    private static double printAVGTime(List<Double> exeTimes) {
        double avg = 0;
        for(int i=0; i<exeTimes.size(); i++)
            avg += exeTimes.get(i);
        /*
            Changing record time div by 1k to get seconds HERE
         */
        //return avg / ((double)exeTimes.size());
        avg = avg / ((double)exeTimes.size());
        return avg / 1000.0;
    }

    private static double printAVGTimeExceptFirstTime(List<Double> exeTimes) {
        double avg = 0;
        for(int i=1; i<exeTimes.size(); i++)
            avg += exeTimes.get(i);
        return avg / ((double)(exeTimes.size()-1));
    }
}
