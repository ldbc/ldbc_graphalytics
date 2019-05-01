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


if [[ $# -ne 2 && $# -ne 5 ]]; then
	echo "Usage: $0 <input-directory> <output-file-basename> [<sort-parallelism> <sort-memory-size> <sort-temp-directory>]"
fi

input_dir="$1"
output_v_file="$2.v"
output_e_file="$2.e"

if [[ $# == 5 ]]; then
	parallelism=$3
	mem_size=$4
	temp_dir="$5"
	sort_cmd() {
		sort --parallel=$parallelism -S $mem_size -T "$temp_dir" "$@"
	}
else
	sort_cmd() {
		sort "$@"
	}
fi

# Generate the edge list
if [[ -e ${output_e_file}.tmp ]]; then
	echo "Temporary file ${output_e_file}.tmp must not exist. Please remove it before restarting this script." >&2
	exit 1
fi
for f in $(ls -1 "$input_dir" | grep -E "^person_knows_person.*csv"); do
	tail -n +2 "$input_dir/$f" | cut -d'|' -f1,2,4 --output-delimiter " " >> ${output_e_file}.tmp
done
sort_cmd -n -k1,1 -k2,2 ${output_e_file}.tmp > $output_e_file
rm ${output_e_file}.tmp

# Generate the vertex list
cut -d' ' -f1,2 "$output_e_file" | tr " " "\n" | sort_cmd -n -u > "$output_v_file"

