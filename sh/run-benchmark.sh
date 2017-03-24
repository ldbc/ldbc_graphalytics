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

rootdir=$(dirname $(readlink -f ${BASH_SOURCE[0]}))/../
config="${rootdir}/config/"

function print-usage() {
	echo "Usage: ${BASH_SOURCE[0]} [--config <dir>]" >&2
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

# Execute platform specific initialization
export config=$config
. ${rootdir}/sh/prepare-benchmark.sh "$@"

# Verify that the platform variable is set
if [ "$platform" = "" ]; then
	echo "The prepare-benchmark.sh script must set variable \$platform" >&2
	exit 1
fi

# Verify that corresponding binary exists
if ! find lib -name "graphalytics-platforms-$platform*.jar" | grep -q '.'; then
	echo "No binary exist in lib/ for platform \"$platform\"" >&2
	exit 1
fi

# Verify that the library jar is set
if [ "$LIBRARY_JAR" = "" ]; then
	echo "The prepare-benchmark.sh script must set variable \$LIBRARY_JAR" >&2
	echo "Fall back to support legacy library jar" >&2
	LIBRARY_JAR=$(find ${rootdir}/lib/graphalytics-platforms-$platform*.jar)
	#exit 1
fi

# Run the benchmark
export CLASSPATH=$config:$(find ${rootdir}/$LIBRARY_JAR):$platform_classpath
java -cp $CLASSPATH $java_opts nl.tudelft.graphalytics.GraphalyticsBenchmark $platform $platform_opts

