#!/bin/sh
#
# Copyright 2015 - 2017 Atlarge Research Team,
# operating at Technische Universiteit Delft
# and Vrije Universiteit Amsterdam, the Netherlands.
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

export platform="${platform-acronym}"

# Set Library jar
export LIBRARY_JAR=`ls lib/graphalytics-*default*.jar`
GRANULA_ENABLED=$(grep -E "^benchmark.run.granula.enabled[	 ]*[:=]" $config/granula.properties | sed 's/benchmark.run.granula.enabled[\t ]*[:=][\t ]*\([^\t ]*\).*/\1/g' | head -n 1)
if [ "$GRANULA_ENABLED" = "true" ] ; then
 if ! find lib -name "graphalytics-*granula*.jar" | grep -q '.'; then
    echo "Granula cannot be enabled due to missing library jar" >&2
 else
    export LIBRARY_JAR=`ls lib/graphalytics-*granula*.jar`
 fi
fi

# Build binaries
if [ -z $PLATFROM_HOME ]; then
    PLATFROM_HOME=`awk -F' *= *' '{ if ($1 == "${platform-acronym}.home") print $2 }' $config/${platform-acronym}.properties`
fi

if [ -z $PLATFROM_HOME ]; then
    echo "Error: home directory for ${platform-name} not specified."
    echo "Define the environment variable \$PLATFROM_HOME or modify ${platform-acronym}.home in $config/${platform-acronym}.properties"
    exit 1
fi

if [ -z $DISABLE_MPI ]; then
    DISABLE_MPI=`awk -F' *= *' '{ if ($1 == "${platform-acronym}.disable_mpi") print $2 }' $config/${platform-acronym}.properties`
fi

DISABLE_JVM=1

mkdir -p bin/standard
(cd bin/standard && cmake -DCMAKE_BUILD_TYPE=Release ../../src/main/c -DPLATFROM_HOME=$PLATFROM_HOME -DNO_JVM=$DISABLE_JVM -DNO_MPI=$DISABLE_MPI && make all)

if [ "$GRANULA_ENABLED" = "true" ] ; then
 mkdir -p bin/granula
 (cd bin/granula && cmake -DCMAKE_BUILD_TYPE=Release -DGRANULA=1 ../../src/main/c -DPLATFROM_HOME=$PLATFROM_HOME -DNO_JVM=$DISABLE_JVM -DNO_MPI=$DISABLE_MPI && make all)
fi


if [ $? -ne 0 ]
then
    echo "compilation failed"
    exit 1
fi
