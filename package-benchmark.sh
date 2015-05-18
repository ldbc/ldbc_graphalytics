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
	echo "Usage: ${BASH_SOURCE[0]} <platform-name> <platform-version> <platform-path>" >&2
}

# Verify the input
if [ $# -ne 3 ]; then
	print-usage
	exit 1
fi

platform_name=$1
platform_version=$2
platform_path=$(readlink -f "$3")

if [ ! -f "${platform_path}/target/graphalytics-platforms-${platform_name}-${platform_version}.jar" ]; then
	echo "Expected to find platform binary at ${platform_path}/target/graphalytics-platforms-${platform_name}-${platform_version}.jar" >&2
	print-usage
	exit 1
fi

# Package benchmark
old_dir=$(pwd)
cd "$(dirname ${BASH_SOURCE[0]})/graphalytics-dist"
mvn package "-Dplatform.name=$platform_name" "-Dplatform.version=$platform_version" "-Dplatform.dir=$platform_path"
cd "$old_dir"

