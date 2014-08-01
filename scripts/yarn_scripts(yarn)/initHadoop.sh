#!/bin/bash 

HADOOP_HOME=$1
CONF=$2
echo "$HADOOP_HOME"
echo "$CONF"

echo "@@@ FORMAT NameNode @@@"
$HADOOP_HOME/bin/hadoop --config $CONF namenode -format

echo "@@@ START HADOOP @@@"
$HADOOP_HOME/sbin/./start-all.sh
#$HADOOP_HOME/bin/./start-dfs.sh
#$HADOOP_HOME/bin/./start-mapred.sh
