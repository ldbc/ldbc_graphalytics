#!/bin/bash

<<<<<<< HEAD
if [ "$#" -lt "1" ]; then
	echo "Usage: ${BASH_SOURCE[0]} <platform>"
=======
if [ "$#" -ge "1" ] && [ "$1" = "-skip" ]; then
	shift
	SKIP_COMPILE=true
fi

if [ "$#" -lt "1" ]; then
	echo "Usage: ${BASH_SOURCE[0]} [-skip] <platform>"
>>>>>>> Finished rework of core and YARN
	exit 1
fi

PLATFORM=$1

cd $(dirname ${BASH_SOURCE[0]})

<<<<<<< HEAD
mvn package install -pl platforms/$PLATFORM -am -Dmaven.repo.local="$(pwd)/.m2/"
mvn -q dependency:build-classpath -pl platforms/$PLATFORM -Dmdep.outputFile="$(pwd)/platforms/$PLATFORM/.maven-classpath" -Dmaven.repo.local="$(pwd)/.m2/"

CLASSPATH=$(find $(pwd)/platforms/$PLATFORM/target/graphalytics-platforms-*-jar-with-dependencies.jar):$(cat platforms/$PLATFORM/.maven-classpath):$HADOOP_HOME/etc/hadoop/
=======
if [ ! "$SKIP_COMPILE" = "true" ]; then
	mvn package install -pl platforms/$PLATFORM -am -Dmaven.repo.local="$(pwd)/.m2/"
	mvn -q dependency:build-classpath -pl platforms/$PLATFORM -Dmdep.outputFile="$(pwd)/platforms/$PLATFORM/.maven-classpath" -Dmaven.repo.local="$(pwd)/.m2/"
fi

CLASSPATH=$(find $(pwd)/platforms/$PLATFORM/target/graphalytics-platforms-*-jar-with-dependencies.jar):$(platforms/$PLATFORM/compute-classpath.sh):$(pwd)/graphs/
>>>>>>> Finished rework of core and YARN

java -cp $CLASSPATH org.tudelft.graphalytics.Graphalytics $PLATFORM
