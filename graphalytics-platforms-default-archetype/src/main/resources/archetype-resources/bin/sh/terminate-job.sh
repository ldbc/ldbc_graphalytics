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

    --job-id)
      JOB_ID="$value"
      shift;;

    --log-path)
      LOG_PATH="$value"
      shift;;

    *)
      echo "Error: invalid option: " "$key"
      exit 1
      ;;
  esac
  shift
done

# TODO Reconstruct executable commandline instructions (platform-specific).
# COMMAND=kill -9 $LOG_PATH/executable.pid


echo "Terminating platform job" "$COMMAND"

$COMMAND