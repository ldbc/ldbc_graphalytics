#!/bin/sh
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

export java_opts="-Xmx${heapsize}m -Xms${heapsize}m -XX:+UseConcMarkSweepGC"

