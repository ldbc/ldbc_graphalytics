import sys
import graphlab as gl

__author__ = 'Jorai Rijsdijk'

if len(sys.argv) < 3:
    print >> sys.stderr, 'Too few arguments, at least 2 required: <graph_name> <expected_output>'
    exit(1)

result_graph = gl.load_sgraph(sys.argv[1])

expected = gl.SFrame.read_csv(sys.argv[2], delimiter=' ', header=False, column_type_hints=long)

for node in result_graph.vertices.sort('__id'):
    test = expected.apply(lambda x: node['component_id'] != x['X2'] and node['__id'] == x['X1'])
    if test.sum() > 0:
        print 'Not all values match, invalid algorithm'
        exit(1)