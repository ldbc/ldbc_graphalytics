#!/bin/bash

YARN_CLASSPATH=$(cat $(dirname ${BASH_SOURCE[0]})/.maven-classpath)

if [ "$HADOOP_HOME" = "" ]
then
	>&2 echo "Graphalytics on YARN (MapReduce v2) requires the HADOOP_HOME environment variable to be set."
	exit 1
fi

YARN_CLASSPATH="$YARN_CLASSPATH:$(readlink -f $HADOOP_HOME/etc/hadoop/)"

echo $YARN_CLASSPATH
exit 0

