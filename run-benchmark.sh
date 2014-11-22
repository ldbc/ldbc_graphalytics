#!/bin/bash

if [ "$#" -ge "1" ] && [ "$1" = "-skip" ]; then
	shift
	SKIP_COMPILE=true
fi

if [ "$#" -lt "1" ]; then
	echo "Usage: ${BASH_SOURCE[0]} [-skip] <platform>"
	exit 1
fi

PLATFORM=$1

cd $(dirname ${BASH_SOURCE[0]})

if [ ! "$SKIP_COMPILE" = "true" ]; then
	mvn package install -pl platforms/$PLATFORM -am -Dmaven.repo.local="$(pwd)/.m2/"
	mvn -q dependency:build-classpath -pl platforms/$PLATFORM -Dmdep.outputFile="$(pwd)/platforms/$PLATFORM/.maven-classpath" -Dmaven.repo.local="$(pwd)/.m2/"
fi

CLASSPATH=$(find $(pwd)/platforms/$PLATFORM/target/graphalytics-platforms-*-jar-with-dependencies.jar):$(platforms/$PLATFORM/compute-classpath.sh):$(pwd)/config/

java -cp $CLASSPATH org.tudelft.graphalytics.Graphalytics $PLATFORM
