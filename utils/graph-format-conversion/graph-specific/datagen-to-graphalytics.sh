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


if [[ $# -ne 3 && $# -ne 6 ]]; then
	echo "Usage: $0 <input-directory> <output-vertex-file> <output-edge-file> [<sort-parallelism> <sort-memory-size> <sort-temp-directory>]"
fi

input_dir=$1
output_v_file=$2
output_e_file=$3

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

# Generate the vertex list
rm -f ${output_v_file}.tmp
for f in $(ls -1 "$input_dir" | grep -E "^person.*csv" | grep -v knows); do
	tail -n +2 $f | cut -d'|' -f1 >> ${output_v_file}.tmp
done
sort_cmd -n ${output_v_file}.tmp > $output_v_file
rm ${output_v_file}.tmp

# Generate the edge list
rm -f ${output_e_file}.tmp
for f in $(ls -1 "$input_dir" | grep -E "^person_knows_person.*csv"); do
	tail -n +2 $f | cut -d'|' -f1,2,4 --output-delimiter " " >> ${output_e_file}.tmp
done
sort_cmd -n -k1,1 -k2,2 ${output_e_file}.tmp > $output_e_file
rm ${output_e_file}.tmp

