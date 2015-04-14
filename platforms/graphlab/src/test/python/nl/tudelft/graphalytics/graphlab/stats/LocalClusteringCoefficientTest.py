import sys
import graphlab as gl

__author__ = 'Jorai Rijsdijk'


def float_not_equals(expected, actual, delta=0.000001):
    return expected - delta > actual or expected + delta < actual


if len(sys.argv) < 3:
    print >> sys.stderr, 'Too few arguments, at least 2 required: <graph_name> <expected_output>'
    exit(1)

result_graph = gl.load_sgraph(sys.argv[1])

expected = gl.SFrame.read_csv(sys.argv[2], delimiter=' ', header=False, column_type_hints=[long, float])

for node in result_graph.vertices.sort('__id'):
    test = expected.apply(
        lambda x: float_not_equals(x['X2'], node['local_clustering_coefficient']) and node['__id'] == x['X1'])
    if test.sum() > 0:
        print 'Not all values match, invalid algorithm'
        exit(1)

expected_average_cc = expected.filter_by([0], 'X1')['X2'][0]
actual_average_cc = result_graph.vertices['average_clustering_coefficient'][0]
if float_not_equals(expected_average_cc, actual_average_cc):
    print 'Average Clustering Coefficient is wrong: expected: "%s", but got: "%s"' % (
        expected_average_cc, actual_average_cc)
