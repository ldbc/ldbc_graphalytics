#!/bin/bash

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
java -cp $CLASSPATH nl.tudelft.graphalytics.Graphalytics $platform $platform_opts

