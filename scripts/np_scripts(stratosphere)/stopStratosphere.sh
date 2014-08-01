#!/bin/bash 

NP_HOME=$1
echo "$NP_HOME"

echo "@@@ STOP STRATOSPHERE @@@"
$NP_HOME/bin/./stop-pact-web.sh
$NP_HOME/bin/./stop-cluster.sh
#$HADOOP_HOME/bin/./stop-dfs.sh
#$HADOOP_HOME/bin/./stop-mapred.sh 
