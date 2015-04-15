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


set -e

rootdir=$(dirname $(readlink -f ${BASH_SOURCE[0]}))
config="${rootdir}/config/"

function print-usage() {
	echo "Usage: ${BASH_SOURCE[0]} [--config <dir>] [--compile] <platform>" >&2
}

# Parse the command-line arguments
while :
do
	case "$1" in
		--config)                      # Use a different config directory
			config="$(readlink -f "$2")"
			echo "Using config: $config"
			shift 2
			;;
		--compile)                     # Compile before running the benchmark
			compile="compile"
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

# Ensure the mandatory arguments are present
if [ "$#" -lt "1" ]; then
	print-usage
	exit 1
fi
platform="$1"

# Change directory to the graphalytics project root
cd $rootdir

# Verify that the project exists
if [ ! -d "platforms/$platform" ]; then
	echo "Unknown platform: $platform" >&2
	print-usage
	exit 1
fi

# Compile the project if requested
if [ "$compile" = "compile" ]; then
	echo "Compiling $platform"
	. compile-benchmark.sh --no-tests -- $platform
fi

# Execute platform specific initialization
export config=$config
. platforms/$platform/prepare-benchmark.sh "$@"

# Run the benchmark
export CLASSPATH=$config:$(find $(pwd)/platforms/$platform/target/graphalytics-platforms-$platform*.jar):$platform_classpath
java -cp $CLASSPATH $java_opts nl.tudelft.graphalytics.Graphalytics $platform $platform_opts

