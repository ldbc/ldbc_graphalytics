#!/bin/bash
#
# Copyright 2015 Delft University of Technology
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


function print-usage() {
	echo "Usage: ${BASH_SOURCE[0]} [--no-tests] [--no-distribution] [--recompile] [<platform>...]" >&2
}

function concat-profiles() {
	if [ $# -gt 1 ]; then
		firstProfile=$1
		shift

		echo "$firstProfile,$(concat-profiles $@)"
	else
		echo "$1"
	fi
}

# Default settings
tests="yes"
distribution="yes"
clean=""

# Parse the command-line flags
while [ $# -gt 0 ]
do
	case "$1" in
		--no-test|--no-tests)          # Skip tests during compilation
			tests="no"
			shift
			;;
		--no-distribution)             # Skip packaging the distribution
			distribution="no"
			shift
			;;
		--recompile)                   # Force recompilation by cleaning
			clean="clean"
			shift
			;;
		--)                            # End of options
			shift
			break
			;;
		-*)                            # Unknown command line option
			echo "Unknown option: $1" >&2
			print-usage
			exit 1
			;;
		*)                             # End of options
			break
			;;
	esac
done

# Parse the platforms to include
while [ $# -gt 0 ]
do
	case "$1" in
		giraph)
			giraph="giraph"
			shift
			;;
		graphlab)
			graphlab="graphlab"
			shift
			;;
		graphx)
			graphx="graphx"
			shift
			;;
		mapreducev2)
			mapreducev2="mapreducev2"
			shift
			;;
		neo4j)
			neo4j="neo4j"
			shift
			;;
		*)
			echo "Unknown platform: $1"
			exit 1
			;;
	esac
done

# Construct the list of profiles to activate, defaulting to all platforms
profiles=$(concat-profiles $giraph $graphlab $graphx $mapreducev2 $neo4j)
if [ "$profiles" == "" ]; then
	profiles="giraph,graphlab,graphx,mapreducev2,neo4j"
fi
if [ "$distribution" = "yes" ]; then
	profiles=$(concat-profiles $profiles "dist")
fi

# Construct maven options
mvnoptions="-P$profiles"
if [ "$tests" = "no" ]; then
	mvnoptions="$mvnoptions -DskipTests"
fi
if [ "$distribution" = "no" ]; then
	mvnoptions="$mvnoptions -DskipDistribution"
fi

# Change directory to the graphalytics project root
cd $(dirname ${BASH_SOURCE[0]})

# Execute the maven command to build
mvn $clean package $mvnoptions
