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

from __future__ import division, print_function
import argparse
import graphlab as gl

__author__ = 'Jorai Rijsdijk'


def parse_args(description):
    """
    Parse the arguments of an algorithm test.

    :param description: The description of the algorithm script
    :return: The result of ArgumentParser.parse_args()
    """
    parser = argparse.ArgumentParser(description=description)
    parser.add_argument("graph_name", help="The name of the result graph to load (relative path)")
    parser.add_argument("expected_output", help="The file with the expected output of the algorithm")
    return parser.parse_args()


def main():
    args = parse_args('Test the result of the BreadFirstSearch algorithm')
    result_graph = gl.load_sgraph(args.graph_name)
    expected = gl.SFrame.read_csv(args.expected_output, delimiter=' ', header=False, column_type_hints=long)

    for node in result_graph.vertices:
        test = expected.apply(lambda x: node['distance'] != x['X2'] and node['__id'] == x['X1'])
        if test.sum() > 0:
            print('Not all values match, invalid algorithm')
            exit(1)

if __name__ == '__main__':
    main()