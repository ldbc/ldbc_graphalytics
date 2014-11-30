#!/bin/bash

if [ "$#" -lt "1" ]; then
	echo "Usage: ${BASH_SOURCE[0]} <platform>"
	exit 1
fi

PLATFORM=$1

cd $(dirname ${BASH_SOURCE[0]})

mvn clean package -P$platform
