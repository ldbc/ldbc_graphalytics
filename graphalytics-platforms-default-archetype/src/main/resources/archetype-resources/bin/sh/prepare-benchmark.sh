#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
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

# Ensure the configuration file exists
if [ ! -f "$config/platform.properties" ]; then
	echo "Missing mandatory configuration file: $config/platform.properties" >&2
	exit 1
fi

# Set library jar
export LIBRARY_JAR=`ls lib/graphalytics-*default*.jar`


# Construct the classpath
PLATFORM_HOME=$(grep -E "^platform.${platform-acronym}.home[	 ]*[:=]" $config/platform.properties | sed 's/platform.${platform-acronym}.home[\t ]*[:=][\t ]*\([^\t ]*\).*/\1/g' | head -n 1)
if [ -z $PLATFORM_HOME ]; then
    echo "Error: ${platform-name} home directory not specified."
    echo "Define variable platform.${platform-acronym}.home in $config/platform.properties"
    exit 1
fi

