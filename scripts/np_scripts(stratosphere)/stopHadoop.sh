#!/bin/bash 

HADOOP_HOME=$1
echo "$HADOOP_HOME"

echo "@@@ STOP HADOOP @@@"
#$HADOOP_HOME/bin/./stop-all.sh
$HADOOP_HOME/bin/./stop-dfs.sh
#$HADOOP_HOME/bin/./stop-mapred.sh 
