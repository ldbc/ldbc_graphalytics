#!/bin/sh

# Ensure the configuration file exists
if [ ! -f "$config/neo4j.properties" ]; then
	echo "Missing mandatory configuration file: $config/neo4j.properties" >&2
	exit 1
fi

# Get the first specification of jvm.heap.size.mb
heapsize=$(grep -E "^jvm\.heap\.size\.mb[	 ]*[:=]" $config/neo4j.properties | sed 's/jvm\.heap\.size\.mb[\t ]*[:=][\t ]*\([^\t ]*\).*/\1/g' | head -n 1)
if [ "h$heapsize" = "h" ]; then
	echo "Invalid definition of jvm.heap.size.mb: $heapsize" >&2
	exit 1
fi
echo "Using heap size: $heapsize"

export platform_opts="-Xmx${heapsize}m -Xms${heapsize}m"

