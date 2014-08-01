package org.testsuite.das4;

import org.apache.log4j.Logger;
import org.hadoop.test.jobs.utils.HadoopJob;
import org.testsuite.exceptions.TestSuiteException;
import org.testsuite.job.Suite;

import java.io.*;
import java.util.List;
import java.util.StringTokenizer;

/*
    Main script builder.
 */
public class BashScriptBuilder {
    static Logger log = Logger.getLogger(BashScriptBuilder.class.getName());

    public void addExeRight() throws IOException{
        Runtime.getRuntime().exec("chmod +x ./scripts/init.sh");
        Runtime.getRuntime().exec("chmod +x ./scripts/initCluster.sh");
        Runtime.getRuntime().exec("chmod +x ./scripts/runHadoop.sh");
        Runtime.getRuntime().exec("chmod +x ./scripts/fetch.sh");
    }

    public void createHadoopInitScript(List<HadoopJob> jobs, Suite suite) throws IOException, TestSuiteException {
        // check required fields
        if(suite.getUser().equals(null))
            throw new TestSuiteException("Field \"user\" required for das execution.");

        File file = new File("./scripts/init.sh");
        GenericScriptBuilder gsb = new GenericScriptBuilder();
        HadoopScriptBuilder hsb = new HadoopScriptBuilder();

        if (file.exists())
            file.delete();

        BufferedWriter script = new BufferedWriter(new FileWriter(file));
        // header
        script.write(gsb.createScriptHeader()+"\n");
        script.newLine();
        // copyToDas
        for(HadoopJob job : jobs) {
            if(job.isInit()) {
                script.write(gsb.copyToDas(suite.getUser(), suite.getPassword(), job.getJobInput(), "/var/scratch/"+suite.getUser()+"/input")+"\n");
                script.newLine();
            }
        }
        // copy run script
        script.write(gsb.copyRunScriptToDas(suite.getUser(), suite.getPassword(), "runHadoop.sh", "/home/"+suite.getUser()+"/scripts")+"\n");
        script.newLine();

        // connect and pass initCluster.sh
        script.write(gsb.connectDasAndExeInitCluster(suite.getUser(), suite.getPassword())+"\n");
        script.newLine();

        // exit
        script.write(gsb.exit());

        script.close();
    }

    public void createHadoopInitClusterScript(List<HadoopJob> jobs, Suite suite) throws IOException, TestSuiteException {
        // check required fields
        if(suite.getUser().equals(null))
            throw new TestSuiteException("Field \"user\" required for das execution.");

        File file = new File("./scripts/initCluster.sh");
        GenericScriptBuilder gsb = new GenericScriptBuilder();
        HadoopScriptBuilder hsb = new HadoopScriptBuilder();

        if (file.exists())
            file.delete();

        BufferedWriter script = new BufferedWriter(new FileWriter(file));
        // header
        script.write(gsb.createScriptHeader()+"\n");
        script.newLine();

        // start runHadoop script in a screen
        script.write(gsb.startRunScriptInScreen(suite.getUser())+"\n");
        script.newLine();

        //record screen session id
        script.write(gsb.recordScreenSessionId(suite.getUser())+"\n");
        script.newLine();

        // exit
        script.write(gsb.exit());

        script.close();
    }

    public void createHadoopRunScript(List<HadoopJob> jobs, Suite suite) throws IOException, TestSuiteException {
        File file = new File("./scripts/runHadoop.sh");
        GenericScriptBuilder gsb = new GenericScriptBuilder();
        HadoopScriptBuilder hsb = new HadoopScriptBuilder();

        if (file.exists())
            file.delete();

        BufferedWriter script = new BufferedWriter(new FileWriter(file));
        // header
        script.write(gsb.createScriptHeader()+"\n");
        script.newLine();

        // start hadoop
        script.write(hsb.startHadoopClusterWithOverwrite(suite.getBlockSize(), suite.getNodeSize()));
        script.newLine();

        script.write(hsb.checkBarriers());
        script.newLine();

        //run jobs
        for(HadoopJob job : jobs) {
            for(int i=0; i<job.getJobRuns(); i++) {
                script.write(hsb.runStatsJob(job, suite.getUser(), i));
                script.newLine();
            }
        }


        script.close();
    }

    public void createHadoopFetchScript(List<HadoopJob> jobs, Suite suite) throws IOException, TestSuiteException {
        File file = new File("./scripts/fetch.sh");
        GenericScriptBuilder gsb = new GenericScriptBuilder();
        HadoopScriptBuilder hsb = new HadoopScriptBuilder();

        /*
        W teori fetch.sh powinnien sie nie zmienic odkomentowac after debugging
        if (file.exists())
            return;
         */
        if (file.exists())
            file.delete();

        BufferedWriter script = new BufferedWriter(new FileWriter(file));
        // header
        script.write(gsb.createScriptHeader()+"\n");
        script.newLine();

        script.write("echo 'Connecting to DAS4 delft site'\n");
        script.write("sshpass -p '"+suite.getPassword()+"' ssh "+suite.getUser()+"k@fs3.das4.tudelft.nl\n");
        script.write("echo 'Checking if test have finished'\n");
        script.write("read -r screenId < /home/"+suite.getUser()+"/scripts/screenId\n");
        script.write("isRunning=`kill -0 $screenId`\n");
        script.write("if [ \"$isRunning\" != \"0\" ]; then\n");
        // tests still running
        script.write("\techo 'Test are still running, will exit now'\n");
        // tests have finished
        script.write("else\n");
        for(HadoopJob job : jobs) {
            if(job.isInit())
                script.write(gsb.copyFromDas(suite.getUser(), suite.getPassword(), "/var/scratch/"+suite.getUser()+"/output/output_"+this.getDatasetName(job)+"*", job.getJobOutput())+"\n");
        }

        script.write("todo shutdown cluster\n");
        // todo doklepac aplikacje dla plotteraStandalone ktora bedzie robic dla dasa ploty

        script.write("fi\n");
        script.write("echo 'DONE :)'\n");

        script.close();
    }

    public String getDatasetName(HadoopJob job) {
        String inputDataset = job.getJobInput();
        StringTokenizer tokenizer = new StringTokenizer(inputDataset, "/"); // linux path
        if(tokenizer.countTokens() < 1)
            tokenizer = new StringTokenizer(inputDataset, "\\"); // windows path
        int tokensNr = tokenizer.countTokens();
        for(int i=0; i<(tokensNr-1); i++)
            tokenizer.nextToken();
        String datasetName = tokenizer.nextToken();
        tokenizer = new StringTokenizer(datasetName, ".");
        if(tokenizer.countTokens() > 1)
            datasetName = tokenizer.nextToken();

        return datasetName;
    }

    public List<HadoopJob> runHadoopJob(List<HadoopJob> jobs, Suite suite) {
        log.info("!! ToDo take under consideration different jobs in a single suite than Stats !!");
        log.info("Creating Hadoop DAS4 asynchronous scripts");
        try {
            // build scripts
            this.createHadoopInitScript(jobs, suite);
            this.createHadoopRunScript(jobs, suite);
            this.createHadoopInitClusterScript(jobs, suite);
            this.createHadoopFetchScript(jobs, suite); // static script no need for dynamic creation
            // add exe rights
            this.addExeRight();

            // invoke init script
            /*ProcessBuilder processBuilder = new ProcessBuilder("./scripts/init.sh");
            processBuilder.redirectErrorStream(true);
            Process initScript = processBuilder.start();

            //redirect childs stream to parent
            BufferedReader chmodOutput = new BufferedReader(new InputStreamReader(initScript.getInputStream()));
            String line;
            while ((line = chmodOutput.readLine()) != null)
                System.out.println("InitScript: " + line);
            initScript.waitFor();*/
        } catch (IOException ex ){
            log.info(ex.getStackTrace());
        } catch (TestSuiteException ex) {
            log.info(ex.getStackTrace());
        } /*catch (InterruptedException ex) {
            log.info(ex.getStackTrace());
        }*/

        return jobs;
    }
}
