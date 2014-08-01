package org.testsuite.das4;

/*
    GENERIC script part, platform independent
    - header "#!/bin/bash"
    - connect to das
    - copy to/from das
    - screen
    - exit
 */
public class GenericScriptBuilder {
    public String createScriptHeader() {
        return new String("#!/bin/bash");
    }

    public String copyToDas(String user, String password, String input, String output) {
        return new String("#Copy Input to DAS\nsshpass -p '"+password+"' scp -rp "+input+" "+user+"@fs3.das4.tudelft.nl:"+output);
    }

    public String copyFromDas(String user, String password, String input, String output) {
        return new String("#Copy OUTPUT to Local\nsshpass -p '"+password+"' scp -rp "+user+"@fs3.das4.tudelft.nl:"+input+" "+output+"\n");
    }

    public String connectDas(String  user, String password) {
        return new String("#Connect to DAS4\nsshpass -p '"+password+"' ssh "+user+"@fs3.das4.tudelft.nl");
    }

    public String connectDasAndExeInitCluster(String  user, String password) {
        return new String("#Connect to DAS4\nsshpass -p '"+password+"' ssh "+user+"@fs3.das4.tudelft.nl 'bash -s' < initCluster.sh");
    }

    public String copyRunScriptToDas(String user, String password, String input, String output) {
        return new String("#Copy run script to das\nsshpass -p '"+password+"' scp -rp "+input+" "+user+"@fs3.das4.tudelft.nl:"+output);
    }

    public String startScreen() {
        return new String("#Starting screen\nscreen");
    }

    public String startRunScriptInScreen(String user) {
        return new String("#Starting run script\necho 'run script invoked'\ncd /home/"+user+"/scripts/\n" +
                "screen -d -m ./runHadoop.sh");
    }

    public String recordScreenSessionId(String user) {
        return new String("#Recording screen session ID\nscreenId=`ps -u "+user+" | grep screen | grep -v grep | awk '{print $1}'`\n" +
                          "echo $screenId > /home/"+user+"/scripts/screenId");
    }

    public String detachScreen() {
        return new String("#Detach screen\nscreen -d");
    }

    public String exit() {
        return "exit";
    }
}
