#!/usr/bin/env bash
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


source_dir="$(dirname "${BASH_SOURCE[0]}")"

# Check for the existence of input parameters
if [[ $# -ne 3 && $# -ne 6 ]]; then
	echo "Usage: $0 <multi-edge-file> <multi-edge-file-delimiter> <output-file-basename> [<sort-parallelism> <sort-memory-size> <sort-temp-directory>]"
	exit -1
fi

# Extract common parameters
input_file=$1
input_delimiter=$2
output_basename=$3

# Prepare the sort command with optional parallelism, memory size, and temporary directory
if [[ $# == 6 ]]; then
	parallelism=$4
	mem_size=$5
	temp_dir="$6"
	sort_cmd() {
		sort --parallel=$parallelism -S $mem_size -T "$temp_dir" "$@"
	}
else
	sort_cmd() {
		sort "$@"
	}
fi

echo "Extracting list of unique vertices..."
cat "$input_file" | tr "$input_delimiter" "\n" | sort_cmd -n -u > "$output_basename.v"
echo "Aggregating multi-edges..."
cat "$input_file" | tr "$input_delimiter" " " | sort_cmd -n -k1,1 -k2,2 | uniq -c | sed 's/^ *//' | awk 'FS=" " { print $2, $3, 1.0 / $1 }' > "$output_basename.e"

