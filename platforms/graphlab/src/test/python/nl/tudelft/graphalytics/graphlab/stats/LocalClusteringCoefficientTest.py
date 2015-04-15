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


def float_not_equals(expected, actual, delta=0.000001):
    return expected - delta > actual or expected + delta < actual


def main():
    args = parse_args('Test the result of the CommunityDetection algorithm')
    result_graph = gl.load_sgraph(args.graph_name)
    expected = gl.SFrame.read_csv(args.expected_output, delimiter=' ', header=False, column_type_hints=long)

    for node in result_graph.vertices.sort('__id'):
        test = expected.apply(
            lambda x: float_not_equals(x['X2'], node['local_clustering_coefficient']) and node['__id'] == x['X1'])
        if test.sum() > 0:
            print('Not all values match, invalid algorithm')
            exit(1)

    expected_average_cc = expected.filter_by([0], 'X1')['X2'][0]
    actual_average_cc = result_graph.vertices['average_clustering_coefficient'][0]
    if float_not_equals(expected_average_cc, actual_average_cc):
        print('Average Clustering Coefficient is wrong: expected: "%s", but got: "%s"' % (
            expected_average_cc, actual_average_cc))