#!/usr/bin/env bash
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


if [[ $# -ne 5 ]]; then
	echo "Usage: $0 <input-edge-list-file> <input-delimiter> <graph-is-directed> <output-vertex-file> <output-edge-file>"
	exit -1
fi

input_file=$1
input_delimiter=$2
graph_is_directed=$3
output_v_file=$4
output_e_file=$5

cat $input_file | tr '\r\n' '\n' | tr "$input_delimiter" '\n' | sort -n -u > $output_v_file
if [[ $graph_is_directed ]]; then
	if [[ $input_delimiter != " " ]]; then
		cat $input_file |
			tr "$input_delimiter" " " |
			tr '\r\n' '\n' |
			awk '{ if ($1 != $2) print $1, $2 }' |
			sort -n -u -k1,1 -k2,2 > $output_e_file
	else
		cat $input_file |
			tr '\r\n' '\n' |
			awk '{ if ($1 != $2) print $1, $2 }' |
			sort -n -u -k1,1 -k2,2 > $output_e_file
	fi
else
	if [[ $input_delimiter != " " ]]; then
		cat $input_file |
			tr '\r\n' '\n' |
			tr "$input_delimiter" " " |
			awk '{ if ($1 < $2) print $1, $2; else if ($1 > $2) print $2, $1 }' |
			sort -n -u -k1,1 -k2,2 > $output_e_file
	else
		cat $input_file |
			tr '\r\n' '\n' |
			awk '{ if ($1 < $2) print $1, $2; else if ($1 > $2) print $2, $1 }' |
			sort -n -u -k1,1 -k2,2 > $output_e_file
	fi
fi

