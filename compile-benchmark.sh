#!/bin/bash

function print-usage() {
	echo "Usage: ${BASH_SOURCE[0]} [--no-tests] [--recompile] <platform>" >&2
}

# Parse the command-line arguments
while :
do
	case "$1" in
		--no-test|--no-tests)          # Skip tests during compilation
			tests="no"
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

# Ensure the mandatory arguments are present
if [ "$#" -lt "1" ]; then
	print-usage
	exit 1
fi
platform="$1"

# Construct maven options
mvnoptions="-P$platform"
if [ "$tests" = "no" ]; then
	mvnoptions="$mvnoptions -DskipTests"
fi

# Change directory to the graphalytics project root
cd $(dirname ${BASH_SOURCE[0]})

# Execute the maven command to build
mvn $clean package $mvnoptions
