#!/bin/bash

YARN_CLASSPATH=$(cat $(dirname ${BASH_SOURCE[0]})/.maven-classpath)

if [ "$HADOOP_HOME" = "" ]
then
	>&2 echo "Graphalytics on YARN (Giraph) requires the HADOOP_HOME environment variable to be set."
	exit 1
fi

HADOOP_CLASSPATH=$(HADOOP_HOME=$(readlink -f $HADOOP_HOME) $HADOOP_HOME/bin/hadoop classpath)
YARN_CLASSPATH="$YARN_CLASSPATH:$HADOOP_CLASSPATH"

echo $YARN_CLASSPATH
exit 0

