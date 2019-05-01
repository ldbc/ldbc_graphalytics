#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
#set( $symbol_bash = '${BASH_SOURCE[0]}' )
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

set -e

rootdir=$(dirname $(readlink -f $symbol_bash))/../..

# Parse commandline instructions (provided by Graphalytics).
while [[ $# -gt 1 ]] # Parse two arguments: [--key value] or [-k value]
  do
  key="$1"
  value="$2"

  case $key in

    --graph-name)
      GRAPH_NAME="$value"
      shift;;

    --input-vertex-path)
      INPUT_VERTEX_PATH="$value"
      shift;;

    --input-edge-path)
      INPUT_EDGE_PATH="$value"
      shift;;

    --output-path)
      OUTPUT_PATH="$value"
      shift;;

    --directed)
      DIRECTED="$value"
      shift;;

    --weighted)
      WEIGHTED="$value"
      shift;;

    *)
      echo "Error: invalid option: " "$key"
      exit 1
      ;;
  esac
  shift
done

# TODO Reconstruct executable commandline instructions (platform-specific).
# mkdir -p $OUTPUT_PATH
# ln -s $INPUT_VERTEX_PATH $OUTPUT_PATH/vertex.csv
# ln -s $INPUT_EDGE_PATH $OUTPUT_PATH/edge.csv
#
# [[ "$DIRECTED" == true ]] && PLATFORM_UNDIRECTED=0 || PLATFORM_UNDIRECTED=1
# [[ "$WEIGHTED" == true ]] && PLATFORM_WEIGHT=1 || PLATFORM_WEIGHT=0
#
# COMMAND="$rootdir/bin/exe/genCSR \
#        --dataset $OUTPUT_PATH --outpath $OUTPUT_PATH \
#        --undirected $PLATFORM_UNDIRECTED --weight $PLATFORM_WEIGHT"

echo "Executing graph loader:" ["$COMMAND"]

$COMMAND