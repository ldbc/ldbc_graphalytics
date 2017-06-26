#!/usr/bin/env python
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

from __future__ import print_function
from itertools import combinations
import argparse
import math
import sys

# Threshold use when comparing two floating-point values
COMPARISON_EPSILON = 0.01


def parse_file(filename, value_parser, verbose):
    results = {}
    errors = 0

    with open(filename) as handle:
        for index, line in enumerate(handle):
            parts = line.strip().split()

            try:
                if parts and not parts[0].startswith('#'):
                    if len(parts) != 2:
                        raise ValueError('invalid number of fields')

                    try:
                        key = int(parts[0])
                    except:
                        raise ValueError('failed to parse vertex id '
                                '("{}" is not a number)'
                                .format(parts[0]))

                    try:
                        value = value_parser(parts[1])
                    except:
                        raise ValueError('failed to parse vertex value '
                                '("{}" is not a value)'
                                .format(parts[1]))

                    if key in results:
                        raise ValueError('duplicate entry for vertex {}'
                                .format(key))

                    results[key] = value

            except ValueError as e:
                if verbose:
                    print('error in {} on line {}: {}'.format(filename,
                        index + 1, e), file=sys.stderr)

                errors += 1

    return results, errors


def check_exact():
    return lambda x, y: x == y


def check_threshold():
    def comparison(x, y):
        global COMPARISON_EPSILON

        # Check exact match first to handle +/-inf
        return x == y or all(abs(x-y) < COMPARISON_EPSILON * v for v in (x, y))

    return comparison


def check_equivalence():
    equiv_x2y = {}
    equiv_y2x = {}

    def comparison(x, y):
        if x not in equiv_x2y:
            equiv_x2y[x] = y

        if y not in equiv_y2x:
            equiv_y2x[y] = x

        return equiv_x2y[x] == y and equiv_y2x[y] == x

    return comparison


def compare_results(file_a, results_a, file_b, results_b, comparison, verbose):
    keys = set()
    keys.update(results_a.keys())
    keys.update(results_b.keys())

    errors = 0

    for key in keys:
        try:
            if key not in results_a:
                raise ValueError('vertex id {} is missing in {}'.format(key, file_a))

            if key not in results_b:
                raise ValueError('vertex id {} not found in {}'.format(key, file_b))

            if not comparison(results_a[key], results_b[key]):
                raise ValueError('{} does not match {} for vertex id {}'.format(
                    results_a[key], results_b[key], key))

        except ValueError as e:
            if verbose:
                print('error comparing {} to {}: {}'.format(file_a, file_b,
                    e), file=sys.stderr)
            errors += 1

    return len(keys) - errors, errors


def get_algorithm_params(algorithm):
    if algorithm in ('bfs', 'cdlp'):
        return int, check_exact
    elif algorithm in ('pr', 'lcc', 'sssp'):
        return float, check_threshold
    elif algorithm in ('wcc',):
        return int, check_equivalence
    else:
        raise ValueError('unknown algorithm: {}'.format(algorithm))


def main():
    algorithms = ('bfs', 'pr', 'wcc', 'cdlp', 'lcc', 'sssp')

    argparser = argparse.ArgumentParser()
    argparser.add_argument('algorithm', metavar='ALGORITHM', type=str,
            choices=algorithms,
            help='Algorithm used to generate output ({})'.format(','.join(algorithms)))
    argparser.add_argument('-v', '--verbose', action='store_true',
            help='Print all errors found while parsing and comparing files')
    argparser.add_argument('files', metavar='FILES', type=str, nargs='*',
            help='Files to compare')
    args = argparser.parse_args()

    results = {}
    parser, checker = get_algorithm_params(args.algorithm)

    for name in args.files:
        results[name], err = parse_file(name, parser, args.verbose)

        if err > 0:
            print('error: {} invalid lines found while parsing {}'.format(err, name))

    total_errors = 0

    for (file1, file2) in sorted(combinations(results.keys(), 2)):
        success, errors = compare_results(
                file1, results[file1],
                file2, results[file2],
                checker(), args.verbose)

        total_errors += errors
        print('comparing {} to {}: {} matching vertices, {} different vertices'.format(file1, file2, success, errors))

    sys.exit(0 if not total_errors else 1)



if __name__ == "__main__":
    main()
