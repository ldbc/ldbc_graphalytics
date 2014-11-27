#!/bin/bash


if [ "$HADOOP_HOME" = "" ]
then
	>&2 echo "Graphalytics on YARN (MapReduce v2) requires the HADOOP_HOME environment variable to be set."
	exit 1
fi

HADOOP_CLASSPATH=$(HADOOP_HOME=$(readlink -f $HADOOP_HOME) $HADOOP_HOME/bin/hadoop classpath)

echo $HADOOP_CLASSPATH
exit 0

