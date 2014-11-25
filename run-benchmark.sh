#!/bin/bash

set -e

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
	. ./compile-benchmark.sh $PLATFORM
fi

CLASSPATH=$(find $(pwd)/platforms/$PLATFORM/target/graphalytics-platforms-*-jar-with-dependencies.jar):$(platforms/$PLATFORM/compute-classpath.sh):$(pwd)/config/
export CLASSPATH=$CLASSPATH

java -cp $CLASSPATH org.tudelft.graphalytics.Graphalytics $PLATFORM
