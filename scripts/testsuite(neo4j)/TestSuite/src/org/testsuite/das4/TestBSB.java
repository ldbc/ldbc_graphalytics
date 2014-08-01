package org.testsuite.das4;

import java.io.*;

public class TestBSB {
    public static void main(String[] args) { // rename public void runHadoop() { ... }
        try {
            //new BashScriptBuilder().createHadoopScript();
            /*TestBSB test = new TestBSB();
            HadoopScriptBuilder hadoopSB = new HadoopScriptBuilder();
            File file = new File("testScript.sh");
            file.createNewFile();
            FileOutputStream fileOutStream = new FileOutputStream(file);
            String hadoop = "";

            hadoop += hadoopSB.cleanLocal("~/output", "fileName", false);
            hadoop += hadoopSB.cleanDfs("/dfsPath", "", true);
            hadoop += hadoopSB.cleanLogs();
            hadoop += hadoopSB.copyDatasetToDFS("~/inputDataSet");
            hadoop += hadoopSB.runStats("dfsIn", "dfsOut");

            fileOutStream.write(hadoop.getBytes());
            fileOutStream.close();

            test.addExeRight2Script();
            //test.exeScript();*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}